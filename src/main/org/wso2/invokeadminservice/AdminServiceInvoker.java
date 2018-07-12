/*
 *
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.wso2.invokeadminservice;

import org.apache.log4j.Logger;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.soap.*;

public class AdminServiceInvoker {
    public static void main(String[] args) {

        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
            public X509Certificate[] getAcceptedIssuers(){return null;}
            public void checkClientTrusted(X509Certificate[] certs, String authType){}
            public void checkServerTrusted(X509Certificate[] certs, String authType){}
        }};

        Logger logger = Logger.getLogger(AdminServiceInvoker.class);
        Properties properties = new Properties();
        InputStream propertiesFile;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            propertiesFile = new FileInputStream("conf/config.properties");
            properties.load(propertiesFile);

            String soapEndpointUrl = properties.getProperty("BackendURL") + "/services/UserAdmin.UserAdminHttpsSoap11Endpoint/";
            String soapAction = "changePassword";
            String adminUserName = properties.getProperty("AdminUserName");
            String adminPassword = properties.getProperty("AdminPassword");
            String userName = properties.getProperty("UserName");
            String newPassword = properties.getProperty("NewPassword");
            long timeInterval = Long.parseLong(properties.getProperty("TimeInterval"));

            SOAPConnectionFactory connectionFactory;
            SOAPConnection connection;

            connectionFactory = SOAPConnectionFactory.newInstance();
            connection = connectionFactory.createConnection();

            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage outgoingMessage = messageFactory.createMessage();
            SOAPPart soappart = outgoingMessage.getSOAPPart();

            SOAPEnvelope envelope = soappart.getEnvelope();

            String nameSpace = "xsd";
            String nameSpaceURI = "http://org.apache.axis2/xsd";
            envelope.addNamespaceDeclaration(nameSpace, nameSpaceURI);

            SOAPBody body = envelope.getBody();
            SOAPElement soapBodyElement = body.addChildElement("changePassword", nameSpace);
            SOAPElement userNameElement = soapBodyElement.addChildElement("userName", nameSpace);
            userNameElement.addTextNode(userName);
            SOAPElement newPasswordElement = soapBodyElement.addChildElement("newPassword", nameSpace);
            newPasswordElement.addTextNode(newPassword);

            String authorization = new BASE64Encoder().encode((adminUserName+":"+adminPassword).getBytes());
            MimeHeaders headers = outgoingMessage.getMimeHeaders();
            headers.addHeader("SOAPAction", soapAction);
            headers.addHeader("Authorization", "Basic " + authorization);

            outgoingMessage.saveChanges();
            ByteArrayOutputStream outputStream;
            while (true) {

                Date requestTime = new Date();
                SOAPMessage soapResponse = connection.call(outgoingMessage, soapEndpointUrl);
                Date responseTime = new Date();
                long executionTime = responseTime.getTime() - requestTime.getTime();
                outputStream = new ByteArrayOutputStream();
                soapResponse.writeTo(outputStream);
                if (!outputStream.toString().contains("<ns:changePasswordResponse xmlns:ns=\"http://org.apache.axis2/xsd\">")) {
                    logger.info("Execution Time:" + executionTime + "ms - " + "Response: " + outputStream.toString());
                }
                TimeUnit.SECONDS.sleep(timeInterval);
            }
        } catch (Exception e) {
            logger.error("Exeption occurred while invoking the admin service. ", e);
        }
    }
}
