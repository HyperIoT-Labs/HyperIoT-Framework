/*
 * Copyright 2019-2023 HyperIoT
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.huser.service;

import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.api.HyperIoTAuthenticationProvider;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;
import it.acsoftware.hyperiot.base.exception.HyperIoTEntityNotFound;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.exception.HyperIoTWrongUserPasswordResetCode;
import it.acsoftware.hyperiot.base.security.annotations.AllowPermissions;
import it.acsoftware.hyperiot.base.security.annotations.AllowPermissionsOnReturn;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntityServiceImpl;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.huser.api.HUserApi;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.mail.api.MailSystemApi;
import it.acsoftware.hyperiot.mail.util.MailConstants;
import it.acsoftware.hyperiot.mail.util.MailUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static it.acsoftware.hyperiot.base.util.HyperIoTConstants.OSGI_AUTH_PROVIDER_RESOURCE;

/**
 * @author Aristide Cittadino Implementation class of the HUserApi interface. It
 * is used to implement all additional methods in order to interact with
 * the system layer.
 */
@Component(service = {HUserApi.class, HyperIoTAuthenticationProvider.class}, immediate = true, property = {
        OSGI_AUTH_PROVIDER_RESOURCE + "=it.acsoftware.hyperiot.huser.model.HUser"
})
public final class HUserServiceImpl extends HyperIoTBaseEntityServiceImpl<HUser> implements HUserApi, HyperIoTAuthenticationProvider {

    /**
     * Injecting the HUserSystemApi to use methods defined in HUserApi interface
     */
    private HUserSystemApi systemService;
    private MailSystemApi mailService;

    /**
     * Constructor for a HUserServiceImpl
     */
    public HUserServiceImpl() {
        super(HUser.class);
    }

    /**
     * @return The current HUserSystemService
     */
    protected HUserSystemApi getSystemService() {
        getLog().debug("invoking getSystemService, returning: {}", this.systemService);
        return systemService;
    }

    /**
     * @param hUserSystemService Injecting via OSGi DS current HUserSystemService
     */
    @Reference
    protected void setSystemService(HUserSystemApi hUserSystemService) {
        getLog().debug("invoking setSystemService, setting: {}", hUserSystemService);
        this.systemService = hUserSystemService;
    }

    /**
     * @return HyperIoT Mail Service
     */
    public MailSystemApi getMailService() {
        return mailService;
    }

    /**
     * @param mailService
     */
    @Reference
    public void setMailService(MailSystemApi mailService) {
        this.mailService = mailService;
    }

    /**
     * Find an existing user by username
     *
     * @param username parameter required to find a user
     * @return the user researched
     */
    @Override
    public HUser findUserByUsername(String username) {
        return systemService.findUserByUsername(username);
    }

    /**
     * This method allows a user registration that will be accessible by
     * unregistered users
     *
     * @param u   parameter required to register a user
     * @param ctx user context of HyperIoT platform
     */
    @Override
    public void registerUser(HUser u, HyperIoTContext ctx) {
        getLog().debug("Invoking registerUser User {} Context: {}", new Object[]{u, ctx});
        if (!HyperIoTUtil.isAccountActivationEnabled()) {
            getLog().warn("User activation is disabled by option : {}", HyperIoTConstants.HYPERIOT_PROPERTY_ACCOUNT_ACTIVATION_ENABLED);
            throw new HyperIoTUnauthorizedException();
        }
        this.systemService.registerUser(u, ctx);
        // if ok sending mail
        List<String> recipients = new ArrayList<>();
        recipients.add(u.getEmail());
        HashMap<String, Object> params = new HashMap<>();
        params.put("username", u.getUsername());
        params.put("activateAccountUrl", HyperIoTUtil.getActivateAccountUrl() + "/" + u.getEmail() + "/" + u.getActivateCode());
        params.put("activationCode", u.getActivateCode());
        try {
            String mailBody = mailService.generateTextFromTemplate(MailConstants.MAIL_TEMPLATE_REGISTRATION, params);
            this.mailService.sendMail(MailUtil.getUsername(), recipients, null, null, "HyperIoT Account Activation!",
                    mailBody, null);
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
        }
    }

    /**
     * Updates user account only if the user is changing his personal info
     */
    @Override
    @AllowPermissions(actions = HyperIoTCrudAction.Names.UPDATE)
    public HUser adminUpdateAccountInfo(HyperIoTContext context, HUser user) {
        HUser loggedUser = this.systemService.findUserByUsername(context.getLoggedUsername());
        if (loggedUser == null) {
            throw new HyperIoTUnauthorizedException();
        }
        return doUpdateAccountInfo(loggedUser, user, context);
    }

    @Override
    public void deleteAccountRequest(HyperIoTContext ctx) {
        if (ctx == null || ctx.getLoggedEntityId() == 0) {
            throw new HyperIoTUnauthorizedException();
        }
        String deletionCode = UUID.randomUUID().toString();
        HUser user = this.systemService.changeDeletionCode(ctx, deletionCode);
        List<String> recipients = new ArrayList<>();
        recipients.add(user.getEmail());
        HashMap<String, Object> params = new HashMap<>();
        params.put("username", user.getUsername());
        params.put("accountDeletionCode", user.getDeletionCode());
        try {
            String mailBody = mailService.generateTextFromTemplate(MailConstants.MAIL_TEMPLATE_ACCOUNT_DELETION, params);
            this.mailService.sendMail(MailUtil.getUsername(), recipients, null, null, "HyperIoT Account Deletion!",
                    mailBody, null);
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
        }
    }

    @Override
    public void deleteAccount(HyperIoTContext ctx, long userId, String deletionCode) {
        if (ctx == null || ctx.getLoggedEntityId() == 0) {
            throw new HyperIoTUnauthorizedException();
        }
        if (ctx.isAdmin()) {
            if (ctx.getLoggedEntityId() == userId) {
                getLog().info("Delete account fail. Admin user with id {},  cannot delete himself", ctx.getLoggedEntityId());
                throw new HyperIoTUnauthorizedException();
            }
            HUser user;
            try {
                user = this.systemService.find(userId, ctx);
            } catch (NoResultException e) {
                throw new HyperIoTEntityNotFound();
            }
            if (user.isAdmin()) {
                getLog().info("Delete account fail. Admin user with id {}, cannot delete admin user with id {} ", ctx.getLoggedEntityId(), user.getId());
                throw new HyperIoTUnauthorizedException();
            }
            //When account deletion is performed by the administrator the deletion code is ignored
            this.systemService.remove(userId, ctx);
            getLog().info("Admin user with id {} , delete account of user with id {} ", ctx.getLoggedEntityId(), user.getId());
        } else {
            if (deletionCode == null) {
                getLog().info("Delete account fail. User with id {}, perform deletion without specifying deletion code", ctx.getLoggedEntityId());
                throw new HyperIoTUnauthorizedException();
            }
            if (ctx.getLoggedEntityId() != userId) {
                getLog().info("Delete account fail. User with id {},  cannot delete user with id {}", ctx.getLoggedEntityId(), userId);
                throw new HyperIoTUnauthorizedException();
            }
            HUser user;
            try {
                user = this.systemService.find(userId, ctx);
            } catch (NoResultException e) {
                throw new HyperIoTEntityNotFound();
            }
            if (user.deletionCode == null || user.deletionCode.isEmpty()) {
                getLog().info("Delete account failed. User with id {}, perform deletion without requesting deletion code", ctx.getLoggedEntityId());
                throw new HyperIoTUnauthorizedException();
            }
            if (!HyperIoTUtil.matchesEncoding(deletionCode, user.getDeletionCode())) {
                getLog().info("Delete account failed. User with id {} insert wrong deletion code", ctx.getLoggedEntityId());
                throw new HyperIoTUnauthorizedException();
            }
            this.systemService.remove(userId, ctx);
            getLog().info("User with id {} , delete himself from platform", userId);
        }
    }

    @Override
    public HUser updateAccountInfo(HyperIoTContext context, HUser user) {
        HUser loggedUser = this.systemService.findUserByUsername(context.getLoggedUsername());
        if (loggedUser.getId() == user.getId()) {
            return doUpdateAccountInfo(loggedUser, user, context);
        }
        throw new HyperIoTUnauthorizedException();
    }

    private HUser doUpdateAccountInfo(HUser loggedUser, HUser user, HyperIoTContext context) {
        loggedUser.setName(user.getName());
        loggedUser.setLastname(user.getLastname());
        loggedUser.setEmail(user.getEmail());
        loggedUser.setUsername(user.getUsername());
        this.systemService.update(loggedUser, context);
        return loggedUser;
    }

    /**
     *
     */
    @Override
    public void activateUser(String email, String activationCode) {
        this.systemService.activateUser(email, activationCode);
    }

    /**
     *
     */
    public void resetPassword(String email, String resetCode, String password, String passwordConfirm) {
        HUser u = this.systemService.findUserByEmail(email);
        if (u != null) {
            if (u.getPasswordResetCode() == null) {
                throw new HyperIoTWrongUserPasswordResetCode();
            }
            if (u.getPasswordResetCode().equals(resetCode)) {
                this.systemService.changePassword(u, password, passwordConfirm);
                return;
            }
            throw new HyperIoTWrongUserPasswordResetCode();
        } else {
            throw new HyperIoTEntityNotFound();
        }
    }

    /**
     *
     */
    public void passwordResetRequest(String email) {
        HUser u = this.systemService.findUserByEmail(email);
        if (u != null) {
            u.setPasswordResetCode(UUID.randomUUID().toString());
            this.systemService.update(u, null);
            List<String> recipients = new ArrayList<>();
            recipients.add(u.getEmail());
            HashMap<String, Object> params = new HashMap<>();
            params.put("username", u.getUsername());
            params.put("changePwdUrl", HyperIoTUtil.getPasswordResetUrl() + "/" + email + "/" + u.getPasswordResetCode());
            params.put("resetPwdCode", u.getPasswordResetCode());
            try {
                String mailBody = mailService.generateTextFromTemplate(MailConstants.MAIL_TEMPLATE_PWD_RESET, params);
                this.mailService.sendMail(MailUtil.getUsername(), recipients, null, null, "Reset Password", mailBody,
                        null);
            } catch (Exception e) {
                getLog().error(e.getMessage(), e);
            }
        } else {
            throw new HyperIoTEntityNotFound();
        }
    }

    @Override
    @AllowPermissions(actions = HyperIoTCrudAction.Names.UPDATE)
    public HUser adminChangePassword(HyperIoTContext context, long userId, String oldPassword, String newPassowrod, String passwordConfirm) {
        HUser loggedUser = this.systemService.findUserByUsername(context.getLoggedUsername());
        if (loggedUser == null)
            throw new HyperIoTUnauthorizedException();
        return doChangePassword(loggedUser, oldPassword, newPassowrod, passwordConfirm, context);
    }

    @Override
    public HUser changePassword(HyperIoTContext context, long userId, String oldPassword, String newPassword,
                                String passwordConfirm) {
        HUser loggedUser = this.systemService.findUserByUsername(context.getLoggedUsername());
        if (loggedUser == null || loggedUser.getId() != userId)
            throw new HyperIoTUnauthorizedException();
        return doChangePassword(loggedUser, oldPassword, newPassword, passwordConfirm, context);
    }

    private HUser doChangePassword(HUser u, String oldPassword, String password, String passwordConfirm, HyperIoTContext context) {
        if (oldPassword != null && password != null && passwordConfirm != null) {
            if (HyperIoTUtil.passwordMatches(oldPassword, u.getPassword())) {
                return this.systemService.changePassword(u, password, passwordConfirm);
            } else {
                throw new HyperIoTRuntimeException("it.acsoftware.hyperiot.error.password.not.match");
            }
        } else {
            throw new HyperIoTRuntimeException("it.acsoftware.hyperiot.error.password.not.null");
        }
    }

    @Override
    @AllowPermissionsOnReturn(actions = HyperIoTCrudAction.Names.FIND)
    public HyperIoTAuthenticable findByUsername(String username) {
        return this.systemService.findUserByUsername(username);
    }

    @Override
    public HyperIoTAuthenticable login(String username, String password) {
        return this.systemService.login(username, password);
    }

    @Override
    public String[] validIssuers() {
        //returning the generic interface, so it matches the rule inside rest filters
        return new String[]{HyperIoTUser.class.getName()};
    }

    /**
     * @param hyperIoTAuthenticable
     * @return
     */
    @Override
    public boolean screeNameAlreadyExists(HyperIoTAuthenticable hyperIoTAuthenticable) {
        return this.systemService.screeNameAlreadyExists(hyperIoTAuthenticable);
    }
}
