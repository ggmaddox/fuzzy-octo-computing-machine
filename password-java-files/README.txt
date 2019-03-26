git clone https://github.com/UCI-Chenli-teaching/2019w-project3-encryption-example.git 

move the java files into the encryption example, then...

mvn clean compile
mvn exec:java -Dexec.mainClass="UpdateSecureCustomerPassword"
mvn exec:java -Dexec.mainClass="UpdateSecureEmployeePassword"
mvn exec:java -Dexec.mainClass="VerifyCustomerPassword"
mvn exec:java -Dexec.mainClass="VerifyEmployeePassword"