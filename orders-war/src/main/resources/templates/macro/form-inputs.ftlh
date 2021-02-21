<#-- This is a wrapper of the spring freemarker template to follow SWE form patterns so validation errors etc work -->
<#import "/spring.ftl" as spring />

<#macro formLabel name label="" required=false hint="" attributes="">
    <label for="${name}">
        <span class="label" ${attributes}>
          <#if label?has_content>
              <@message 'label.' + label label />
          <#else >
              <@message 'label.' + name name />
          </#if>
        </span>
        <#if required><abbr class="required" title="(required)">*</abbr></#if>
        <#if hint?has_content>
            <small class="hint">${hint}</small></#if>
        <@springErrors />
    </label>
</#macro>

<#macro formRadioLabel name label="" required=false hint="" attributes="">
    <legend for="${name}">
        <span class="label" ${attributes}>
              <#if label?has_content><@message 'label.' + label label /><#else ><@message 'label.' + name "NOT_SET" /></#if></span>
        <#if required><abbr class="required" title="(required)">*</abbr></#if>
        <#if hint?has_content><small class="hint">${hint}</small></#if>
        <@springErrors />
    </legend>
</#macro>

<#macro springErrors>
    <#list spring.status.errorMessages as error> <em class="alert">${error}</em> </#list>
</#macro>

<#macro formSingleSelect form name label="" options={} required=false hint="" disabled=false inputclass="" liclass="" liid="" extra="" attributes=''>
    <@spring.bind form+'.'+name />
    <#if (spring.status.errorMessages?size > 0) >
        <li class="invalid<#if liclass?has_content> ${liclass}</#if>" <#if liid?has_content>id="${liid}"</#if>>
    <#else>
        <li <#if liclass?has_content>class="${liclass}" </#if> <#if liid?has_content>id="${liid}"</#if>>
    </#if>
    <fieldset>
    <@formLabel name label required hint/>
    <@spring.formSingleSelect path=form+'.'+name
    options=options
    attributes=' class="form-control '+inputclass+'" '+required?string(' required="true"', '')+disabled?string(' disabled="disabled"', '')+attributes/>
    </fieldset>
    </li>
</#macro>

<#macro formRadioButtons form name label="" options={} separator="</li><li>" liclass="" disabled=false inputclass="" liid="" required=false extra=""  attributes="">
    <@spring.bind form+'.'+name />
    <#if (spring.status.errorMessages?size > 0) ><li class="invalid<#if liclass?has_content> ${liclass}</#if>" <#if liid?has_content>id="${liid}"</#if>>
    <#else><li <#if liclass?has_content>class="${liclass}" </#if> <#if liid?has_content>id="${liid}"</#if>></#if>
    <fieldset>
        <@formRadioLabel name label required />
        <#if extra??><#noautoesc><@messageText code='label.' + extra text=extra escape=false /></#noautoesc></#if>
        <ul class="choices">
            <li>
                <@formRadioButtonsCustomized
                path=form+"."+name
                options=options
                separator=separator
                attributes=' class="' + inputclass + '" ' + disabled?string(' disabled="disabled"', '') + attributes
                required=required/>
            </li>
        </ul>
    </fieldset>
    </li>
</#macro>

<#--
 * formRadioButtonsCustomized
 *
 * Show radio buttons.
 *
 * @param path the name of the field to bind to
 * @param options a map (value=label) of all the available options
 * @param separator the html tag or other character list that should be used to
 *    separate each option. Typically '&nbsp;' or '<br>'
 * @param attributes any additional attributes for the element (such as class
 *    or CSS styles or size
-->
<#macro formRadioButtonsCustomized path options separator attributes="" required=false>
    <@spring.bind path/>
    <#list options?keys as value>
        <#assign id="${spring.status.expression?replace('[','')?replace(']','')}${value_index}"/>
        <#if value?is_first>
            <input type="radio" id="${id}" name="${spring.status.expression}" value="${value}"<#if spring.stringStatusValue == value> checked="checked"</#if> <#noautoesc>${attributes}</#noautoesc><#if required == true>required="true"</#if><@spring.closeTag/>
        <#else>
            <input type="radio" id="${id}" name="${spring.status.expression}" value="${value}"<#if spring.stringStatusValue == value> checked="checked"</#if> <#noautoesc>${attributes}</#noautoesc><@spring.closeTag/>
        </#if>
            <label for="${id}"><@message 'label.' + options[value] options[value] /></label>
        <#if value?has_next ><#noautoesc>${separator}</#noautoesc></#if>
    </#list>
</#macro>

<#macro formCheckboxes  form name label="" options={} separator="</li><li>" required=false liclass="" liid="">
    <@spring.bind form+'.'+name />
    <#if (spring.status.errorMessages?size > 0) >
        <li class="invalid<#if liclass?has_content> ${liclass}</#if>" <#if liid?has_content>id="${liid}"</#if>>
    <#else>
        <li <#if liclass?has_content>class="${liclass}" </#if> <#if liid?has_content>id="${liid}"</#if>>
    </#if>
        <fieldset>
            <legend>
                <@formLabel name label required />
            </legend>
            <ul class="choices">
                <li>
                    <@formCheckboxesFromSpring  path=form+"."+name options=options separator=separator attributes='class="form-control"' />
                </li>
            </ul>
        </fieldset>
    </li>
</#macro>


<#--
 * formCheckboxes
 *
 * Show checkboxes.
 *
 * @param path the name of the field to bind to
 * @param options a map (value=label) of all the available options
 * @param separator the HTML tag or other character list that should be used to
 *    separate each option (typically '&nbsp;' or '<br>')
 * @param attributes any additional attributes for the element
 *    (such as class or CSS styles or size)
-->
<#macro formCheckboxesFromSpring path options separator attributes="">
    <@spring.bind path/>
    <#list options?keys as value>
        <#assign id="${status.expression?replace('[','')?replace(']','')}${value_index}">
        <#assign isSelected = contains(status.actualValue?default([""]), value)>
    <input type="checkbox" id="${id}" name="${status.expression}" value="${value}"<#if isSelected> checked="checked"</#if> <#noautoesc>${attributes}</#noautoesc><@closeTag/>
    <label for="${id}">${options[value]}</label><#if value?has_next ><#noautoesc>${separator}</#noautoesc></#if>
    </#list>
    <input type="hidden" name="_${status.expression}" value="on"/>
</#macro>

<#macro formCheckboxSingle  form name label="" required=false liclass="" liid="">
    <@spring.bind form+'.'+name />
    <#if (spring.status.errorMessages?size > 0) >
        <li class="invalid<#if liclass?has_content> ${liclass}</#if>" <#if liid?has_content>id="${liid}"</#if>>
    <#else>
        <li <#if liclass?has_content>class="${liclass}" </#if> <#if liid?has_content>id="${liid}"</#if>>
    </#if>
    <@spring.formCheckbox path=form+"."+name attributes='class="checkbox-inline"' />
    <@formLabel name label required />
        </li>
</#macro>

<#macro formHiddenInput form name attributes="">
    <@spring.formInput fieldType="hidden" path=form+'.'+name attributes=attributes />
</#macro>

<#macro formInput form name label="" fieldType="text" required=false maxlength="255" size="20" hint="" pattern="" disabled=false liclass="" liid="" type="text" placeholder="" onblur="" inputclass="" postfix="" okIconRequired=false labelEnabled=true hidden=false>
    <@spring.bind form+'.'+name />
    <#if (spring.status.errorMessages?size > 0) >
            <li class="invalid<#if liclass?has_content> ${liclass}</#if>" <#if liid?has_content>id="${liid}"</#if> <#if hidden>hidden="hidden" aria-hidden="true"</#if> >
    <#else>
            <li <#if liclass?has_content>class="${liclass}" </#if> <#if liid?has_content>id="${liid}"</#if> <#if hidden>hidden="hidden" aria-hidden="true"</#if> >
    </#if>
    <#if labelEnabled ><@formLabel name label required hint/></#if>
    <#if okIconRequired ><br /></#if>

    <@spring.formInput fieldType=fieldType path=form+'.'+name attributes='maxlength="'+maxlength+'" class="form-control '+inputclass+'" size="'+size+'"'+required?string(' required="true"', '')+disabled?string(' disabled="disabled"', '')+hidden?string(' disabled="disabled"', '')+(onblur?has_content)?string(' onblur="'+onblur+'" ', '')+(pattern?has_content)?string(' pattern="'+pattern+'" ', '') />${postfix}
    <#if okIconRequired >
        <span class="ok-tick-icon-blue" aria-hidden="true">
            <i id="ok-${name}" class="fa fa-check-circle qg-visually-hidden ok-tick-icon-blue"></i>
        </span>
    </#if>
        </li>
</#macro>

<#macro formTableInput form name label="" fieldType="text" required=false maxlength="255" size="20" hint="" pattern="" disabled=false liclass="" liid="" type="text" placeholder="" onblur="" inputclass="" postfix="" okIconRequired=false labelEnabled=true hidden=false>
    <@spring.bind form+'.'+name />
    <#if (spring.status.errorMessages?size > 0) >
            <li class="invalid<#if liclass?has_content> ${liclass}</#if>" <#if liid?has_content>id="${liid}"</#if> <#if hidden>hidden="hidden" aria-hidden="true"</#if> >
    <#else>
            <li <#if liclass?has_content>class="${liclass}" </#if> <#if liid?has_content>id="${liid}"</#if> <#if hidden>hidden="hidden" aria-hidden="true"</#if> >
    </#if>
    <#if labelEnabled ><@formLabel name label required hint/></#if>
    <#if okIconRequired ><br /></#if>

    <@spring.formInput fieldType=fieldType path=form+'.'+name attributes='maxlength="'+maxlength+'" class="form-control '+inputclass+'" size="'+size+'"'+required?string(' required="true"', '')+disabled?string(' disabled="disabled"', '')+hidden?string(' disabled="disabled"', '')+(onblur?has_content)?string(' onblur="'+onblur+'" ', '')+(pattern?has_content)?string(' pattern="'+pattern+'" ', '') />${postfix}
    <#if okIconRequired >
        <span class="ok-tick-icon-blue" aria-hidden="true">
            <i id="ok-${name}" class="fa fa-check-circle hidden ok-tick-icon-blue"></i>
        </span>
    </#if>
        </li>
</#macro>

<#macro createDescriptionList form items dlclass="" dlid="">
    <dl class="${dlclass}" id="${dlid}">
    <#list items?keys as key>
            <@spring.bind form+'.'+key />
        <dt id="${key}">
            <@message 'label.' + key items[key] />
        </dt><dd>
    <#if spring.stringStatusValue = "" >
        &nbsp;
    <#else>
        <@message 'label.' + key  + '.' + spring.stringStatusValue spring.stringStatusValue />
    </#if>
    </dd>
    </#list>
    </dl>
</#macro>

<#macro message code default="N/A" >${springMacroRequestContext.getMessage(code, default)}</#macro>

<#--text = defaultMessage-->
<#macro messageArgsText code, args, text, escape>
    <#noautoesc>${springMacroRequestContext.getMessage(code, args, text, escape)}</#noautoesc>
</#macro>

<#--text = defaultMessage-->
<#macro messageText code, text, escape>
    <#noautoesc>${springMacroRequestContext.getMessage(code, [""], text, escape)}</#noautoesc>
</#macro>
