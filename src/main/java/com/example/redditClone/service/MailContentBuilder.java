package com.example.redditClone.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
public class MailContentBuilder {

    private final TemplateEngine templateEngine;

    //takes email message as input
    public String build(String message) {
        Context context = new Context();
        context.setVariable("message", message);//pass the message in html context
        return templateEngine.process("mailTemplate", context);//pass the html name
    }
}
