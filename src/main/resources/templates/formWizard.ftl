<div class="form-cell" ${elementMetaData!}>
    <#if !(request.getAttribute(className)??) >
        <link href="${request.contextPath}/plugin/${className}/css/formWizard.css" rel="stylesheet" type="text/css" />
        <link href="${request.contextPath}/plugin/${className}/css/stepbar-progress.css" rel="stylesheet" type="text/css" />
        <script type="text/javascript" src="${request.contextPath}/plugin/${className}/js/jquery.formWizard.js"></script>
    </#if>
    
    <#if element.properties.css??>
        <style type="text/css">
            ${element.properties.css!}
        </style>
    </#if>
    <div id="${elementParamName!}_formWizard_${element.properties.elementUniqueKey!}" class="form-element formWizard ${element.properties.displayMode!}">
        
        <input type="hidden" class="cPageNum" name="${elementParamName!}_c_page_num" value="${cPageNum!?html}" />
        <input type="hidden" class="changePage" name="${elementParamName!}_change_page" value="" />

        <div class="wizard-progressbar">
            <ul class="wizard-steps">
            <#assign mobileCss="before">
            <#list element.children as e>
                <#if e.className == '${formWizardChildClassName}' >
                    <#assign errorCss="">
                    <#if e.hasError(formData) >
                        <#assign errorCss="error">
                    </#if>

                    <#assign current = (cPageNum! == e.properties.pageNum!?number) >
                    <#assign completed = (e.properties.pageNum!?number < cPageNum!?number) >
                    <li class="wizard-step
                               <#if current>active</#if>
                               <#if completed>completed</#if>
                               <#if errorCss == 'error'>error</#if>">

                        <button <#if current>disabled</#if>
                                <#if elementMetaData! != "" && !current>disabled</#if>
                                rel="${e.properties.pageNum!?number}">

                            <span class="step-circle">${e.properties.pageNum!?number}</span>
                            <span class="step-label">${e.properties.label!?html}</span>
                        </button>
                    </li>
                </#if>
            </#list>
            </ul>
        </div>

        <div class="page-container">
            <#list element.children as e>
                <#if e.className == '${formWizardChildClassName}' >
                    <#if cPageNum! == e.properties.pageNum!?number >
                        <div class="page_${e.properties.pageNum!?number} current">
                            ${e.render(formData, includeMetaData!false)}
                        </div>
                    <#else>
                        <div class="page_${e.properties.pageNum!?number}" style="display:none;">
                            ${e.render(formData, includeMetaData!false)}
                        </div>
                    </#if>
                <#else>
                    <div class="page_key_container" style="display:none;">
                        ${e.render(formData, includeMetaData!false)}
                    </div>
                </#if>
            </#list>
        </div>

        <div class="page-nav-panel bottom">
            <ul>
            <#assign mobileCss="before">
            <#list element.children as e>
                <#if e.className == '${formWizardChildClassName}' >
                    <#assign errorCss="">
                    <#if e.hasError(formData) >
                        <#assign errorCss="error">
                    </#if>
                    <#if cPageNum! == e.properties.pageNum!?number>
                        <#assign mobileCss="after">
                        <li class="nav_item current ${errorCss}"><button disabled><span>${e.properties.label!?html}</span></button></li>
                    <#else>
                        <li class="nav_item ${errorCss} ${mobileCss}"><button <#if elementMetaData! != "">disabled</#if> rel="${e.properties.pageNum!?number}"><span>${e.properties.label!?html}</span></button></li>
                    </#if>
                </#if>
            </#list>
            </ul>
            <div style="clear:both;"></div>
        </div>

        <div class="page-button-panel">
            <input type="submit" class="page-button-prev" name="${elementParamName!}_prev_page" value="${element.properties.prevButtonlabel!?html}" <#if (cPageNum == 1)>hidden</#if> />
            <input type="submit" class="page-button-next" name="${elementParamName!}_next_page" value="${element.properties.nextButtonlabel!?html}" <#if elementMetaData! != "">disabled</#if> <#if (cPageNum >= totalPage)>hidden</#if>/>
        </div>

    </div>
    <script type="text/javascript">
        $(document).ready(function(){
        	$("#${elementParamName!}_formWizard_${element.properties.elementUniqueKey!}").formWizard();
        });
    </script>
</div>