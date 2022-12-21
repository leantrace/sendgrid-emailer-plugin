package com.example.curity.SendGridEmailSender;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class Main {

     static public void main (String... args) {
         Properties p = new Properties();
         try {
             p.load(new StringReader(
                     """
             sendgridTemplateId=abc
activationUrl=${_activationUrl}
token=${token}
oq=!${oq}

             """.stripIndent()
             ));
         } catch (IOException e){
             e.printStackTrace();
         }
         p.forEach((key, value) -> System.out.println(key + "=" + value));

         if (p.containsKey("sendgridTemplateId")){
             System.out.println("args = " + args);
         }

     }
}
