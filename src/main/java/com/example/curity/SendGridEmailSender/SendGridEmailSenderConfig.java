/*
 * Copyright (C) 2021 Curity AB. All rights reserved.
 *
 * The contents of this file are the property of Curity AB.
 * You may not copy or use this file, in either source code
 * or executable form, except in compliance with terms
 * set by Curity AB.
 *
 * For further information, please contact Curity AB.
 */
package com.example.curity.SendGridEmailSender;

import se.curity.identityserver.sdk.config.Configuration;
import se.curity.identityserver.sdk.config.annotation.Description;
import se.curity.identityserver.sdk.service.ExceptionFactory;

public interface SendGridEmailSenderConfig extends Configuration
{
    @Description("Sendgrid API Key.")
    String getSendGridApiKey();

    @Description("The email address that will be used as the from address when sending emails.")
    String getDefaultSender();

    ExceptionFactory getExceptionFactory();
}
