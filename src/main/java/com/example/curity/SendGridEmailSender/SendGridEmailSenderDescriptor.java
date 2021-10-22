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

import se.curity.identityserver.sdk.email.Emailer;
import se.curity.identityserver.sdk.plugin.descriptor.EmailProviderPluginDescriptor;

public final class SendGridEmailSenderDescriptor implements EmailProviderPluginDescriptor<SendGridEmailSenderConfig>
{
    @Override
    public Class<? extends Emailer> getEmailSenderType()
    {
        return SendGridEmailSender.class;
    }

    @Override
    public String getPluginImplementationType()
    {
        return "sendgrid-email-sender";
    }

    @Override
    public Class<? extends SendGridEmailSenderConfig> getConfigurationType()
    {
        return SendGridEmailSenderConfig.class;
    }
}
