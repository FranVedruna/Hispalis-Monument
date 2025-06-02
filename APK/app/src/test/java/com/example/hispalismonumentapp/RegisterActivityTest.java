package com.example.hispalismonumentapp;

import static org.junit.Assert.*;

import com.example.hispalismonumentapp.activities.RegisterActivity;

import org.junit.Test;


public class RegisterActivityTest {

    @Test
    public void test_AllFieldsEmpty() {
        String result = RegisterActivity.validateInputs("", "", "");
        assertEquals("Por favor, complete todos los campos", result);
    }

    @Test
    public void test_NameEmpty() {
        String result = RegisterActivity.validateInputs("", "test@email.com", "password123");
        assertEquals("Por favor, complete todos los campos", result);
    }

    @Test
    public void test_EmailEmpty() {
        String result = RegisterActivity.validateInputs("Juan", "", "password123");
        assertEquals("Por favor, complete todos los campos", result);
    }

    @Test
    public void test_PasswordEmpty() {
        String result = RegisterActivity.validateInputs("Juan", "test@email.com", "");
        assertEquals("Por favor, complete todos los campos", result);
    }

    @Test
    public void test_InvalidEmail_NoAt() {
        String result = RegisterActivity.validateInputs("Juan", "correo.invalido.com", "password123");
        assertEquals("Ingrese un correo electrónico válido", result);
    }

    @Test
    public void test_InvalidEmail_MissingDomain() {
        String result = RegisterActivity.validateInputs("Juan", "correo@", "password123");
        assertEquals("Ingrese un correo electrónico válido", result);
    }

    @Test
    public void test_ShortPassword() {
        String result = RegisterActivity.validateInputs("Ana", "ana@email.com", "123");
        assertEquals("La contraseña debe tener al menos 8 caracteres", result);
    }

    @Test
    public void test_ValidInput() {
        String result = RegisterActivity.validateInputs("Ana", "ana@email.com", "password123");
        assertNull(result);
    }
}