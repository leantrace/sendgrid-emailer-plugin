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
package io.curity.SendGridEmailSender;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.ASM;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.data.email.RenderableEmail;
import se.curity.identityserver.sdk.email.Emailer;
import se.curity.identityserver.sdk.errors.ErrorCode;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

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
    public void sendEmail(RenderableEmail renderableEmail, String recipient) throws IOException {
        try {
            _logger.info(renderableEmail.renderPlainText());
            Properties p = new Properties();
            try {
                p.load(new StringReader(renderableEmail.renderPlainText()));
            } catch (IOException e){/*ignore */}
            SendGrid sg = new SendGrid(_sendGridAPIKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            Personalization personalization = new Personalization();
            Mail mail = new Mail();
            Email to = new Email(recipient);
            personalization.addTo(to);
            mail.setFrom(new Email(_configuration.getDefaultSender()));
            mail.setSubject(renderableEmail.getSubject());
            if (p.containsKey("sendgridTemplateId")){
                String templateId = p.getProperty("sendgridTemplateId");
                _logger.info("Send Sendgrid using sendgrid template: {}", templateId);
                mail.setTemplateId(templateId);
                if (p.containsKey("sendgridAsmId")) {
                    try {
                        int sendgridAsmId = Integer.parseInt(p.getProperty("sendgridAsmId"));
                        ASM asm = new ASM();
                        asm.setGroupId(sendgridAsmId);
                        asm.setGroupsToDisplay(new int[]{sendgridAsmId});
                        mail.setASM(asm);
                    } catch (NumberFormatException e) {
                        _logger.warn("Invalid ASM ID, value must be an integer, but currently is={}", p.getProperty("sendgridAsmId"));
                    }
                }
                p.forEach((k, v) -> personalization.addDynamicTemplateData(k.toString(), v.toString()));
            } else {
                _logger.info("Send Sendgrid using curity template");
                mail.addContent(new Content("text/plain", renderableEmail.renderPlainText()));
                mail.addContent(new Content("text/html", renderableEmail.render()));
                mail.setSubject(renderableEmail.getSubject());
            }
            mail.addPersonalization(personalization);
            request.setBody(mail.build());
            Response response = sg.api(request);

            _logger.info(response.getBody());
            _logger.info(response.getStatusCode()+"");

            if (response.getStatusCode() == 202) {
                _logger.info("Sent email to {} using Sendgrid mailer {}", recipient, _configuration.id());
            } else {
                _logger.info("Failed to send email using Sendgrid");
                throw _configuration.getExceptionFactory().internalServerException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Failed to send email using Sendgrid");
            }
        } catch (IOException e) {
            throw new IOException("Failed to send email using Sendgrid", e);
        }
    }
}
