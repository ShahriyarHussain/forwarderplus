package com.lazoft.forwarderplus.service;

import com.vaadin.flow.component.textfield.TextFieldBase;

public interface ValidationService {

    <T extends TextFieldBase<T, String>>void validateField(T field);
}
