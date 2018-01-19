/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kecak.enterprise;

import java.util.ArrayList;
import java.util.Map;
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
public class MultiPagedFormKeyContainer extends Element implements FormContainer{

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String html = "";
        for (Element child : this.getChildren(formData)) {
            html = html + child.render(formData, Boolean.valueOf(false));
        }
        return html;
    }

    public String getName() {
        return "Multi Paged Form Key Container";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Multi Paged Form Key Container";
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
        if (key != null && !key.isEmpty() && (keyElement = FormUtil.findElement((String)key, (Element)(rootForm = FormUtil.findRootForm((Element)this)), (FormData)formData)) == null) {
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
