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
package io.curity.plugin.sendgrid;

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
import se.curity.identityserver.sdk.service.ExceptionFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SendGridEmailSender implements Emailer
{
    private final String _sendGridAPIKey;
    private static final Logger _logger = LoggerFactory.getLogger(SendGridEmailSender.class);
    private final boolean _sendTemplatedEmails;
    private final ExceptionFactory _exceptionFactory;
    private static final String SENDGRID_TEMPLATE_ID = "sendgridTemplateId";
    private static final String SENDGRID_ASM_ID = "sendgridAsmId";
    private final String _defaultSender;
    private final String _defaultOrganization;
    private final String _pluginId;

    public SendGridEmailSender(SendGridEmailSenderConfig configuration)
    {
        _sendTemplatedEmails = configuration.getSendTemplatedEmails();
        _exceptionFactory = configuration.getExceptionFactory();
        _sendGridAPIKey = configuration.getSendGridApiKey();
        _defaultSender = removeEmailComment(configuration.getDefaultSender());
        _defaultOrganization = extractEmailComment(configuration.getDefaultSender());
        _pluginId = configuration.id();
    }

    private String extractEmailComment(String email) {
        try {
            Pattern pattern = Pattern.compile("\\(([^)]*)\\)");
            Matcher matcher = pattern.matcher(email);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) { /*ignore*/}
        return null;
    }

    private String removeEmailComment(String email) {
        return email.replaceAll("\\([^)]*\\)", "");
    }

    @Override
    public void sendEmail(RenderableEmail renderableEmail, String recipient) throws IOException
    {
        try
        {
            _logger.debug(renderableEmail.renderPlainText());
            Mail mail = new Mail();
            Personalization personalization = new Personalization();
            Properties p = new Properties();
            String templateId = null;
            try
            {
                p.load(new StringReader(renderableEmail.renderPlainText()));
                templateId = p.getProperty(SENDGRID_TEMPLATE_ID);
            }
            catch (IOException e)
            {
                _logger.warn("The authenticator is configured to use templated emails but the template used " +
                        "cannot be parsed as properties. Try loading Template");
                //throw _exceptionFactory.internalServerException(ErrorCode.CONFIGURATION_ERROR, "template is not in the correct format");
            }
            if (templateId != null) {
                _logger.debug("Send Sendgrid using sendgrid template: {}", templateId);
                mail.setTemplateId(templateId);
                if (p.containsKey(SENDGRID_ASM_ID))
                {
                    try
                    {
                        int sendgridAsmId = Integer.parseInt(p.getProperty(SENDGRID_ASM_ID));
                        ASM asm = new ASM();
                        asm.setGroupId(sendgridAsmId);
                        asm.setGroupsToDisplay(new int[]{sendgridAsmId});
                        mail.setASM(asm);
                    }
                    catch (NumberFormatException e)
                    {
                        _logger.warn("Invalid ASM ID, value must be an integer, but currently is={}", p.getProperty(SENDGRID_ASM_ID));
                    }
                }
                p.forEach((k, v) -> {
                    if (k.toString().equals("subject") && _defaultOrganization != null) {
                        String vv = v.toString()+" "+_defaultOrganization;
                        personalization.addDynamicTemplateData(k.toString(), vv);
                    } else {
                        personalization.addDynamicTemplateData(k.toString(), v.toString());
                    }
                });
            }
            else
            {
                _logger.debug("Send Sendgrid using curity template");
                mail.addContent(new Content("text/plain", renderableEmail.renderPlainText()));
                mail.addContent(new Content("text/html", renderableEmail.render()));
                mail.setSubject(renderableEmail.getSubject());
            }

            SendGrid sg = new SendGrid(_sendGridAPIKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");

            Email to = new Email(recipient);
            personalization.addTo(to);
            mail.setFrom(new Email(_defaultSender));
            mail.addPersonalization(personalization);
            request.setBody(mail.build());
            Response response = sg.api(request);

            _logger.debug(response.getBody());
            _logger.debug(response.getStatusCode() + "");

            if (response.getStatusCode() == 202)
            {
                _logger.debug("Sent email to {} using Sendgrid mailer {}", recipient, _pluginId);
            }
            else
            {
                _logger.debug("Failed to send email using Sendgrid");
                throw _exceptionFactory.internalServerException(ErrorCode.EXTERNAL_SERVICE_ERROR, "Failed to send email using Sendgrid");
            }
        }
        catch (IOException e)
        {
            throw new IOException("Failed to send email using Sendgrid", e);
        }
    }
}
