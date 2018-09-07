/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kinnara.kecakplugins.formwizard;

import java.util.ArrayList;
import java.util.Map;

import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.form.lib.HiddenField;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;

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

    public String getName() {
        return AppPluginUtil.getMessage("formWizard.title", getClassName(), "/message/formWizard") + " Key Container";
    }

    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    public String getLabel() {
        return getName();
    }

    public String getClassName() {
        return this.getClass().getName();
    }

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
            keyElement.setProperty("id", (Object)key);
            keyElement.setProperty("useDefaultWhenEmpty", (Object)"true");
            keyElement.setProperty("readonly", (Object)"true");
            keyElement.setParent((Element)this);
            children.add(keyElement);
            this.setChildren(children);
        }
    }
    
}
