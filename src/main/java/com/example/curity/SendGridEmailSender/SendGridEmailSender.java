/*
 *  Copyright 2021 Curity AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.example.curity.SendGridEmailSender;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.data.email.RenderableEmail;
import se.curity.identityserver.sdk.email.Emailer;
import se.curity.identityserver.sdk.errors.ErrorCode;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;

public final class SendGridEmailSender implements Emailer
{
    private final SendGridEmailSenderConfig _configuration;
    private final String _sendGridAPIKey;
    private static final Logger _logger = LoggerFactory.getLogger(SendGridEmailSender.class);

    public SendGridEmailSender(SendGridEmailSenderConfig configuration)
    {
        _configuration = configuration;
        _sendGridAPIKey = configuration.getSendGridApiKey();
    }

    @Override
    public void sendEmail(RenderableEmail renderableEmail, String recipient) throws IOException
    {
        Personalization personalization = new Personalization();
        Mail mail = new Mail();
        if(isValidEmailAddress(_configuration.getDefaultSender())) {
            Email to = new Email(recipient);
            personalization.addTo(to);
            mail.addContent(new Content("text/plain", renderableEmail.renderPlainText()));
            mail.addContent(new Content("text/html", renderableEmail.render()));
            mail.setFrom(new Email(_configuration.getDefaultSender()));
            mail.setSubject(renderableEmail.getSubject());
            mail.addPersonalization(personalization);
        }

        SendGrid sg = new SendGrid(_sendGridAPIKey);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            if (response.getStatusCode()==202)
            {
                _logger.debug("Sent email to {} using Sendgrid mailer {}", recipient, _configuration.id());
            }
            else
            {
                throw _configuration.getExceptionFactory().internalServerException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Unable to send email using Sendgrid");
            }

        } catch (IOException ex) {
            throw _configuration.getExceptionFactory().internalServerException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Unable to send email using Sendgrid");
        }
    }

    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }
}
