/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kinnara.kecakplugins.formwizard;

import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.form.lib.HiddenField;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;

import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Yonathan
 */
public class FormWizardKeyContainer extends Element implements FormContainer{

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        StringBuilder html = new StringBuilder();
        for (Element child : this.getChildren(formData)) {
            html.append(child.render(formData, false));
        }
        return html.toString();
    }

    @Override
    public String getName() {
        return "Form Wizard Key Container";
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }
    
    public void addKeyElement(String key, FormData formData) {
        Form rootForm;
        Element keyElement;
        if (key != null && !key.isEmpty() && (keyElement = FormUtil.findElement(key, rootForm = FormUtil.findRootForm(this), formData)) == null) {
            ArrayList<Element> children = (ArrayList<Element>) this.getChildren(formData);
            if (children == null) {
                children = new ArrayList<Element>();
            }
            keyElement = new HiddenField();
            keyElement.setProperty("id", key);
            keyElement.setProperty("useDefaultWhenEmpty", "true");
            keyElement.setProperty("readonly", "true");
            keyElement.setParent(this);
            children.add(keyElement);
            this.setChildren(children);
        }
    }
    
}
