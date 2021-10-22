package com.example.curity.SendGridEmailSender

import se.curity.identityserver.sdk.data.email.RenderableEmail
import se.curity.identityserver.sdk.service.ExceptionFactory
import spock.lang.Specification

class SendgridEmailSenderTest extends Specification {

    def env = System.getenv()
    def approvedSenderAddress = env['FROM_EMAIL']
    def apiKey = env['API_KEY']
    def recipient = env['TO_EMAIL']

    def "valid from email"() {
        given:
        def config = new TestConfiguration(
                apiKey,
                approvedSenderAddress,
                Mock(ExceptionFactory)
        )

        when:
        def isValid = SendGridEmailSender.isValidEmailAddress(config._defaultSender)

        then:
        isValid == true
    }

    def "send email"() {
        given:
        def config = new TestConfiguration(
                apiKey,
                approvedSenderAddress,
                Mock(ExceptionFactory)
        )

        def sender = new SendGridEmailSender(config)
        def email = getEmail()
        def recipient = recipient

        when:
        sender.sendEmail(email, recipient)

        then:
        "Check for email"
    }

    def "incorrect api key"() {
        given:
        def config = new TestConfiguration(
                "some-bogus-api-key",
                approvedSenderAddress,
                Mock(ExceptionFactory)
        )

        def sender = new SendGridEmailSender(config)
        def email = getEmail()
        def recipient = recipient

        when:
        sender.sendEmail(email, recipient)

        then:
        thrown(RuntimeException)
    }

    def "non-approved from address"() {
        given:
        def config = new TestConfiguration(
                apiKey,
                "non-registered-email-sender@example.com",
                Mock(ExceptionFactory)
        )

        def sender = new SendGridEmailSender(config)
        def email = getEmail()
        def recipient = recipient

        when:
        sender.sendEmail(email, recipient)

        then:
        thrown(RuntimeException)
    }

    def "missing recipient"() {
        given:
        def config = new TestConfiguration(
                apiKey,
                approvedSenderAddress,
                Mock(ExceptionFactory)
        )

        def sender = new SendGridEmailSender(config)
        def email = getEmail()
        def recipient = null

        when:
        sender.sendEmail(email, recipient)

        then:
        thrown(RuntimeException)
    }

    private def getEmail()
    {
        def email = Stub(RenderableEmail)
        email.renderPlainText() >> "Plain text email message"
        email.render() >> "HTML email message"
        email.getSubject() >> "Email subject"
        email
    }

    private class TestConfiguration implements SendGridEmailSenderConfig {

        def _apiKey
        def _defaultSender
        def _exceptionFactory

        TestConfiguration(def apiKey, def defaultSender, def exceptionFactory)
        {
            _apiKey = apiKey
            _defaultSender = defaultSender
            _exceptionFactory = exceptionFactory
        }

        @Override
        String getSendGridApiKey() {
            return _apiKey
        }

        @Override
        String getDefaultSender() {
            return _defaultSender
        }

        @Override
        ExceptionFactory getExceptionFactory() {
            return _exceptionFactory
        }

        @Override
        String id() {
            return "TestConfiguration"
        }
    }
}


