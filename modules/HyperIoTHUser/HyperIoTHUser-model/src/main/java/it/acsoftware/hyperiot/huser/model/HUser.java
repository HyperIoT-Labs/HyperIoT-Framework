package it.acsoftware.hyperiot.huser.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModelProperty;
import it.acsoftware.hyperiot.base.api.HyperIoTRole;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;
import it.acsoftware.hyperiot.base.validation.*;
import it.acsoftware.hyperiot.role.model.Role;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Aristide Cittadino Model class for HUser of HyperIoT platform. It is
 *         used to map HUser with the database.
 *
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "username"),
		@UniqueConstraint(columnNames = "email") })
@PasswordMustMatch
@ValidPassword
public class HUser extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity, HyperIoTUser {
	/**
	 * String name for HUser
	 */
	@JsonView(HyperIoTJSONView.Public.class)
	private String name;
	/**
	 * String lastname for HUser
	 */
	@JsonView(HyperIoTJSONView.Public.class)
	@ApiModelProperty(required = false)
	private String lastname;
	/**
	 * String username for HUser
	 */
	@JsonView(HyperIoTJSONView.Public.class)
	private String username;
	/**
	 * String password for HUser
	 */
	@JsonView(HyperIoTJSONView.Internal.class)
	private String password;
	/**
	 * Boolean admin for HUser
	 */
	@JsonProperty(access = Access.READ_ONLY)
	private boolean admin;
	/**
	 * String passwordConfirm for HUser
	 */
	@JsonView(HyperIoTJSONView.Internal.class)
	private String passwordConfirm;
	
	/**
	 * 
	 */
	@JsonView(HyperIoTJSONView.Public.class)
	private String passwordResetCode;
	
	/**
	 * String email for HUser
	 */
	@JsonView(HyperIoTJSONView.Public.class)
	private String email;
	/**
	 * List of roles for HUser
	 */
	@JsonView(HyperIoTJSONView.Public.class)
	private Set<Role> roles;

	/**
	 * Boolean which indicate that the user is active or not
	 */
	@JsonView(HyperIoTJSONView.Internal.class)
	private boolean active;
	
	/**
	 * Code for activation
	 */
	@JsonView(HyperIoTJSONView.Internal.class)
	private String activateCode;

	/**
	 *  Code for deletion
	 */
	@JsonView(HyperIoTJSONView.Internal.class)
	public String deletionCode;

	/**
	 * the image path of the user account
	 */
	private String imagePath;

	/**
	 * Constructor for HUser. Provides information about HUser class
	 *
	 */
	public HUser() {
		this.roles = new HashSet<Role>();
	}

	/**
	 * Gets the HUser name
	 * 
	 * @return a string that represents HUser name
	 */
	@Column
	@NotNullOnPersist
	@NotEmpty
	@NoMalitiusCode
	@Size(max = 255)
	@ApiModelProperty(required = false)
	public String getName() {
		return name;
	}

	/**
	 * Sets the HUser name
	 * 
	 * @param name contains the HUser name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the HUser lastname
	 * 
	 * @return a string that represents HUser lastname
	 */
	@Column
	@NotNullOnPersist
	@NotEmpty
	@NoMalitiusCode
	@Size( max = 255)
	@ApiModelProperty(required = false)
	public String getLastname() {
		return lastname;
	}

	/**
	 * Sets the HUser lastname
	 * 
	 * @param lastname contains the HUser lastname
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	/**
	 * Gets the HUser username given by a range of alphanumeric characters
	 * 
	 * @return a string that represents HUser username
	 */
	@Column
	@NotNullOnPersist
	@NotEmpty
	@NoMalitiusCode
	@Size( max = 255)
	@Pattern(regexp = "^[A-Za-z0-9]+$",message = "Allowed characters are letters (lower and upper cases) and numbers")
	@ApiModelProperty(required = false)
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the HUser username
	 * 
	 * @param username contains the HUser username
	 */
	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the HUser password encrypted.
	 * 
	 * @return a string that represents HUser password encrypted
	 * Password not null is checked by @ValidPassword on HUser Class
	 */
	@Column
	@NotNullOnPersist
	@NoMalitiusCode
	@ApiModelProperty(required = false)
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the HUser password
	 * 
	 * @param password contains the HUser password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the HUser password confirmation.
	 * 
	 * @return a string that represents HUser password confirmation
	 */
	@Transient
	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	/**
	 * Sets the HUser password confirmation
	 * 
	 * @param passwordConfirm contains the HUser password confirmation
	 */
	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	@JsonProperty(access = Access.WRITE_ONLY)
	public String getPasswordResetCode() {
		return passwordResetCode;
	}

	public void setPasswordResetCode(String passwordResetCode) {
		this.passwordResetCode = passwordResetCode;
	}

	/**
	 * Gets the HUser email
	 * 
	 * @return a string that represents HUser email
	 */
	@Column
	@NotNullOnPersist
	@NotEmpty
	@NoMalitiusCode
	@Email
	@Size( max = 255)
	@ApiModelProperty(required = false)
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the HUser email
	 * 
	 * @param email contains the HUser email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets if HUser is administrator
	 * 
	 * @return true if HUser is administrator
	 */
	@Column
	public boolean isAdmin() {
		return admin;
	}

	/**
	 * Sets the HUser admin
	 * 
	 * @param admin contains the value if a HUser is admin
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 * Gets image path of this user.
	 * @return the user account image path
	 */
	@Length( max = 255)
	@NoMalitiusCode
	public String getImagePath() {
		return imagePath;
	}

	/**
	 * Sets image path of this area map.
	 * @param imagePath user account image path.
	 */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	/**
	 * 
	 * @return true if the user is active
	 */
	@Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
	@ApiModelProperty(required = false)
	public boolean isActive() {
		return active;
	}

	/**
	 * 
	 * @param active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * 
	 * @return Code generated for activation
	 */
	@ApiModelProperty(required = false)
	public String getActivateCode() {
		return activateCode;
	}

	/**
	 * 
	 * @param activateCode
	 */
	public void setActivateCode(String activateCode) {
		this.activateCode = activateCode;
	}

	/**
	 * @return Code generated for account deletion
	 */
	@ApiModelProperty(required = false)
	public String getDeletionCode() {
		return deletionCode;
	}

	public void setDeletionCode(String deletionCode) {
		this.deletionCode = deletionCode;
	}

	/**
	 * Gets the user roles
	 * 
	 * @return a list that represents the user roles
	 */
	@ManyToMany(targetEntity = Role.class, fetch = FetchType.EAGER)
	@JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
	public Set<Role> getRoles() {
		return roles;
	}

	/**
	 * Sets the user roles
	 * 
	 * @param roles a list that contains the user roles
	 */
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	@Override
	public boolean addRole(HyperIoTRole role) {
		if (role instanceof Role)
			return roles.add((Role) role);
		return false;
	}

	@Override
	public boolean hasRole(long id) {
		return roles.stream().filter(role -> role.getId() == id).findAny().isPresent();
	}

	@Override
	public boolean hasRole(String roleName) {
		return roles.stream().filter(role -> role.getName().equalsIgnoreCase(roleName)).findAny()
				.isPresent();
	}

	@Override
	public boolean hasRole(HyperIoTRole role) {
		return this.roles.contains(role);
	}

	@Override
	public boolean removeRole(HyperIoTRole role) {
		return this.roles.remove(role);
	}

	@Override
	@Transient
	@ApiModelProperty(hidden = true)
	@JsonView(HyperIoTJSONView.Internal.class)
	public String getScreenName() {
		return this.getUsername();
	}

	@Transient
	@ApiModelProperty(hidden = true)
	@JsonView(HyperIoTJSONView.Internal.class)
	@Override
	public String getScreenNameFieldName() {
		return "username";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HUser other = (HUser) obj;
		if (this.getId() == 0 && other.getId() > 0 || this.getId() > 0 && other.getId() == 0) {
			if (email == null) {
				if (other.email != null)
					return false;
			} else if (!email.equals(other.email))
				return false;
			if (username == null) {
				if (other.username != null)
					return false;
			} else if (!username.equals(other.username))
				return false;
			return true;
		} else {
			return this.getId() == other.getId();
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ID: ").append(this.getId()).append(" \n User:").append(username);
		return sb.toString();
	}

}