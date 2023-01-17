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

import se.curity.identityserver.sdk.config.Configuration;
import se.curity.identityserver.sdk.config.annotation.DefaultBoolean;
import se.curity.identityserver.sdk.config.annotation.Description;
import se.curity.identityserver.sdk.service.ExceptionFactory;

public interface SendGridEmailSenderConfig extends Configuration
{
    @Description("Sendgrid API Key.")
    String getSendGridApiKey();

    @Description("The email address that will be used as the from address when sending emails.")
    String getDefaultSender();

    ExceptionFactory getExceptionFactory();

    @DefaultBoolean(false)
    @Description("If enabled requires the template to be in *.properties format and contain sendgridTemplateId")
    boolean getSendTemplatedEmails();
}
