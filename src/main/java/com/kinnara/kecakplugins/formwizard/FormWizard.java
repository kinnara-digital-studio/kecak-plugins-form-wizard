package com.kinnara.kecakplugins.formwizard;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.SubForm;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.util.WorkflowUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 *
 * @author Yonathan
 */
public class FormWizard extends FormButton implements FormBuilderPaletteElement, PluginWebSupport, FormContainer, AceFormElement, AdminLteFormElement {

    private int currentPageNumber = 0;
    private boolean partiallyStoreError = false;

    @Override
    public String getName() {
        return "Form Wizard";
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
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        String[] args = {getClassName()};
        return AppUtil.readPluginResource(getClassName(), "/properties/formWizard.json", args, true, "/message/formWizard");
    }

    @Override
    public String getFormBuilderCategory() {
        return "Kecak";
    }

    @Override
    public int getFormBuilderPosition() {
        return 300;
    }

    @Override
    public String getFormBuilderIcon() {
        return "/plugin/" + SubForm.class.getName() + "/images/subForm_icon.gif";
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>" + getName() + "</label>";
    }

    @Override
    public Collection<Element> getChildren(FormData formData) {
        Collection<Element> children = super.getChildren();
        if (formData != null && (children == null || children.isEmpty())) {
            Map<String, Object> numberOfPageObject = (Map<String, Object>) getProperty("numberOfPage");
            if (numberOfPageObject != null) {
                int numberOfPage = Integer.parseInt(numberOfPageObject.get("className").toString());
                Map pagesProperties = (Map) numberOfPageObject.get("properties");
                FormWizardKeyContainer container = new FormWizardKeyContainer();
                container.setParent(this);
                int pageCount = 0;
                for (int page = 1; page <= numberOfPage; ++page) {
                    String label = (String) pagesProperties.get("page" + page + "_label");
                    String formDefId = (String) pagesProperties.get("page" + page + "_formDefId");
                    String readonly = (String) pagesProperties.get("page" + page + "_readonly");
                    String parentSubFormId = (String) pagesProperties.get("page" + page + "_parentSubFormId");
                    String subFormParentId = (String) pagesProperties.get("page" + page + "_subFormParentId");
                    String validate = (String) pagesProperties.get("page" + page + "_validate");
                    if (!checkForRecursiveForm(this, formDefId)) {
                        continue;
                    }
                    FormWizardChild pageElement = getChildPage(label, formDefId, readonly, parentSubFormId, subFormParentId, validate, Integer.toString(++pageCount));
                    children.add(pageElement);
                    container.addKeyElement(parentSubFormId, formData);
                }
                children.add(container);
                setProperty("totalPage", Integer.toString(pageCount));
            } else {
                setProperty("totalPage", "0");
            }
        }
        return children;
    }

    @Override
    public boolean continueValidation(FormData formData) {
        // check for validation on back Prev
        boolean noValidationOnPrev = "true".equalsIgnoreCase(getPropertyString("noValidationOnPrev"));
        return !(noValidationOnPrev && (prevButtonClicked(formData) || getTargetPageNumber(formData) < currentPageNumber));
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String paramName = FormUtil.getElementParameterName(this);
        int pageNum = getCurrentPageNumber(formData);
        int totalPage = Integer.parseInt(getPropertyString("totalPage"));

        boolean hasError;
        if ("true".equals(getPropertyString("child_validate_page_" + currentPageNumber))) {
            hasError = getChildren(formData).stream()
                    .filter(c -> c.getPropertyString("pageNum").equals(String.valueOf(currentPageNumber)))
                    .anyMatch(c -> c.hasError(formData) || partiallyStoreError);
        } else {
            hasError = false;
        }

        if (!hasError && nextButtonClicked(formData)) {
            pageNum = pageNum + 1;
            if (pageNum <= totalPage) {
                currentPageNumber = pageNum;
            }
        } else if (!hasError && prevButtonClicked(formData)) {
            pageNum = pageNum - 1;
            if (pageNum > 0) {
                currentPageNumber = pageNum;
            }
        } else if (!hasError && tabButtonClicked(formData)) {
            currentPageNumber = Integer.parseInt(formData.getRequestParameter(paramName + "_change_page"));
        }
        dataModel.put("cPageNum", getCurrentPageNumber(formData));
        if ("true".equals(getPropertyString("onlyAllowSubmitOnLastPage")) && totalPage != getCurrentPageNumber(formData)) {
            hideParentFormButton(null);
        }

        dataModel.put("totalPage", Optional.ofNullable(getPropertyString("totalPage")).map(Integer::parseInt).orElse(0));

        dataModel.put("className", getClassName());
        dataModel.put("formWizardChildClassName", FormWizardChild.class.getName());
        String template = "formWizard.ftl";
        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }



    public FormWizardChild getChildPage(String label, String formDefId, String readonly, String parentSubFormId, String subFormParentId, String validate, String pageNum) {
        FormWizardChild page = new FormWizardChild();
        page.setProperty("label", label);
        page.setProperty("formDefId", formDefId);
        page.setProperty("readonly", readonly);
        page.setProperty("parentSubFormId", parentSubFormId);
        page.setProperty("subFormParentId", subFormParentId);
        page.setProperty("validate", validate);
        setProperty("child_validate_page_" + pageNum, validate);
        page.setProperty("pageNum", pageNum);
        page.setProperty("id", getPropertyString("id") + "_" + pageNum);
        page.setParent(this);
        return page;
    }

    protected boolean checkForRecursiveForm(Element e, String id) {
        Form form = FormUtil.findRootForm(e);
        if (form != null && form != e) {
            String formId = form.getPropertyString("id");
            if (id.equals(formId)) {
                return false;
            }
            return checkForRecursiveForm(form, id);
        }
        return true;
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isAdmin = WorkflowUtil.isCurrentUserInRole("ROLE_ADMIN");
        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String action = request.getParameter("action");
        if ("getJson".equals(action)) {
            String value = request.getParameter("value");
            int number = Integer.parseInt(value);
            StringBuilder output = new StringBuilder("[");
            for (int i = 1; i <= number; ++i) {
                String pageNumber = Integer.toString(i);
                Object[] arguments = new String[]{pageNumber, pageNumber, pageNumber, pageNumber, pageNumber, pageNumber, pageNumber, pageNumber, pageNumber};
                output.append(AppUtil.readPluginResource(getClass().getName(), "/properties/formWizardChild.json", arguments, true, "message/formWizard")).append(",");
            }
            output = new StringBuilder(output.substring(0, output.length() - 1) + "]");
            response.getWriter().write(output.toString());
        } else {
            response.setStatus(204);
        }
    }

    public FormData actionPerformed(Form form, FormData formData) {
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        FormUtil.executeValidators(this, formData);
        if ("true".equalsIgnoreCase(getPropertyString("partiallyStore")) && formData.getFormResult("_PREVIEW_MODE") == null) {
            partiallyStoreError = false;
            Integer pageNum = getCurrentPageNumber(formData);
            for (Element e : getChildren(formData)) {
                if (!pageNum.toString().equals(e.getPropertyString("pageNum")) || e.hasError(formData)) {
                    continue;
                }
                formData = FormUtil.executeElementFormatData(form, formData);
                try {
                    if ("true".equalsIgnoreCase(getPropertyString("storeMainFormOnPartiallyStore"))) {
                        FormStoreBinder binder = form.getStoreBinder();
                        FormRowSet rows = formData.getStoreBinderData(binder);
                        binder.store(form, rows, formData);
                    }
                    FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");
                    formData = formService.storeElementData(form, e, formData);
                    e.setProperty("validate", "true");
                } catch (Exception ex) {
                    String formId = FormUtil.getElementParameterName(form);
                    formData.addFormError(formId, "Error storing data: " + ex.getMessage());
                    partiallyStoreError = true;
                    LogUtil.error(FormService.class.getName(), ex, "Error executing store binder");
                }
                break;
            }
        }
        return formData;
    }

    @Override
    public boolean isActive(Form form, FormData formData) {
        Integer pageNum = getCurrentPageNumber(formData);
        Integer totalPage = Integer.parseInt(getPropertyString("totalPage"));
        boolean changePage = (nextButtonClicked(formData) && pageNum <= totalPage)
                || (prevButtonClicked(formData) && pageNum > 0)
                || (tabButtonClicked(formData));

        if (changePage) {
            formData.setStay(true);
            setProperty("changePage", "true");
        }

        return changePage;
    }

    protected boolean nextButtonClicked(FormData formData) {
        if(formData == null)
            return false;

        String paramName = FormUtil.getElementParameterName(this);
        return formData.getRequestParameter(paramName + "_next_page") != null;
    }

    protected boolean prevButtonClicked(FormData formData) {
        if(formData == null)
            return false;

        String paramName = FormUtil.getElementParameterName(this);
        return formData.getRequestParameter(paramName + "_prev_page") != null;
    }

    protected boolean tabButtonClicked(FormData formData) {
        if(formData == null)
            return false;

        String paramName = FormUtil.getElementParameterName(this);
        return formData.getRequestParameter(paramName + "_change_page") != null
                && !formData.getRequestParameter(paramName + "_change_page").isEmpty();
    }

    protected int getCurrentPageNumber(FormData formData) {
        if(formData != null && currentPageNumber == 0) {
            String paramName = FormUtil.getElementParameterName((Element) this);
            String cPageNum = formData.getRequestParameter(paramName + "_c_page_num");
            if (cPageNum == null) {
                cPageNum = "1";
            }
            currentPageNumber = Integer.parseInt(cPageNum);
        }
        return currentPageNumber;
    }

    protected int getTargetPageNumber(FormData formData) {
        if(prevButtonClicked(formData) && currentPageNumber > 1) {
            return currentPageNumber - 1;
        }

        if(tabButtonClicked(formData)) {
            String paramName = FormUtil.getElementParameterName(this);
            return Integer.parseInt(formData.getRequestParameter(paramName + "_change_page"));
        }

        return currentPageNumber;
    }

    protected void hideParentFormButton(Element element) {
        if (element == null) {
            element = FormUtil.findRootForm(this);
        }
        if (element != null && element != this) {
            Collection childs;
            if (element instanceof FormButton) {
                LogUtil.info(getClassName(), "Disabling button ["+element.getPropertyString("id")+"]");
                FormUtil.setReadOnlyProperty(element);
//                element.setProperty("disabled", "true");
            }
            if ((childs = element.getChildren()) != null && !childs.isEmpty()) {
                for (Object e : childs) {
                    hideParentFormButton((Element)e);
                }
            }
        }
    }

    @Override
    public Collection<String> getDynamicFieldNames() {
        ArrayList<String> fieldNames = new ArrayList<String>();
        Object numberOfPageObject = getProperty("numberOfPage");
        if (numberOfPageObject instanceof Map) {
            Map temp = (Map) numberOfPageObject;
            int numberOfPage = Integer.parseInt(temp.get("className").toString());
            Map pagesProperties = (Map) temp.get("properties");
            for (int page = 1; page <= numberOfPage; ++page) {
                String parentSubFormId = (String) pagesProperties.get("page" + page + "_parentSubFormId");
                if (parentSubFormId == null || parentSubFormId.isEmpty()) {
                    continue;
                }
                fieldNames.add(parentSubFormId);
            }
        }
        return fieldNames;
    }
}
