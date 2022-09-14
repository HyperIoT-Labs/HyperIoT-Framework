# CREATE SELF CERTS

To create ONLY server certs keystore and truststore:

https://unix.stackexchange.com/questions/347116/how-to-create-keystore-and-truststore-using-self-signed-certificate


To create CLIENT certs:

openssl genrsa -out alice.key 2028
openssl req -new -key alice.key -out alice.csr
openssl x509 -sha256 -req -in alice.csr -out alice.crt -CA CA.pem -CAkey CA.key -CAcreateserial -days 1095
