<#macro page columns=true admin=false fullscreen=false hidetitle=false className="">
    <!DOCTYPE html>
    <html lang="en">
    <head>

        <#assign qgov_assets_url="https://static.qgov.net.au/assets/v2" />
        <#global local_assets_url="${contextPath!'/seniors-concessions'}/static" />

        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <!-- Version: ${gitVersion!} - ${gitLastBuiltTime!} -->

        <title>${title!} <#if formTitle?has_content> | ${formTitle} </#if> | Queensland Government</title>

        <meta name="description" content="DESCRIPTION">
        <meta name="keywords" content="KEYWORDS">

        <link nonce="${__csp_nonce}" rel="schema.DCTERMS" href="http://purl.org/dc/terms/">
        <link nonce="${__csp_nonce}" rel="schema.AGLSTERMS" href="http://www.agls.gov.au/agls/terms/">

        <meta name="DCTERMS.creator" scheme="AGLSTERMS.GOLD" content="c=AU; o=The State of Queensland; ou=DEPARTMENT NAME; ou=UNIT NAME">
        <meta name="DCTERMS.publisher" scheme="AGLSTERMS.AglsAgent" content="corporateName=The State of Queensland; jurisdiction=Queensland">
        <meta name="DCTERMS.created" content="2018-11-04">
        <meta name="DCTERMS.modified" content="2019-06-10">
        <meta name="DCTERMS.title" content="${title!} <#if formTitle?has_content> | ${formTitle} </#if>">
        <meta name="DCTERMS.alternative" content="${title!} <#if formTitle?has_content> | ${formTitle} </#if>">
        <meta name="DCTERMS.description" content="DESCRIPTION">
        <meta name="DCTERMS.subject" scheme="AGLSTERMS.APAIS" content="SUBJECT">
        <meta name="AGLSTERMS.function" scheme="AGLSTERMS.AGIFT" content="FUNCTION">
        <meta name="DCTERMS.type" scheme="DCTERMS.DCMIType" content="Text">
        <meta name="AGLSTERMS.documentType" scheme="AGLSTERMS.agls-document" content="guidelines">
        <meta name="DCTERMS.audience" scheme="AGLSTERMS.agls-audience" content="">
        <meta name="DCTERMS.jurisdiction" scheme="AGLSTERMS.AglsJuri" content="Queensland">

        <#include "../web-template-release/template-cdn-ssi/assets/includes-cdn/head-assets.html"/>

        <link nonce="${__csp_nonce}" rel="stylesheet" href="/style/lato/css/lato.css">
        <link nonce="${__csp_nonce}" rel="stylesheet" href="/style/seniors-global.css">
        <link nonce="${__csp_nonce}" rel="stylesheet" href="/style/swe-patch.css">

        <#noautoesc>
            ${head!}
        </#noautoesc>

        <noscript>
            <style nonce="${__csp_nonce}">
                .collapse{
                    display: block !important;
                }
            </style>
        </noscript>

    </head>

    <body data-qg-accessibility="true">

    <#include "../web-template-release/template-cdn-ssi/assets/includes-cdn/analytics.html">
    <#include "../web-template-release/template-cdn-ssi/assets/includes-cdn/header/access.html">

    <#include "../web-template-release/template-cdn-ssi/assets/includes-cdn/header/header-without-location.html">

    <#include "../partials/breadcrumbs.ftlh">
    <#--
    <#include "../web-template-release/template-cdn-ssi/assets/includes-cdn/breadcrumbs/breadcrumb-demo.html">
    -->

    <div class="container-fluid qg-site-width">


        <div id="qg-content">

            <div id="<#if columns>qg-three-col<#else>qg-two-col-aside</#if>" class="row wide">

                <#if nav_title_key == "title.nav.checkYourEligibility">
                    <#include "../partials/banner.ftlh">
                </#if>

                <!-- left-hand column -->
                <#include "../partials/left-hand-nav.ftlh"/>

                <div id="qg-primary-content" role="main">

                    <#include "../web-template-release/template-cdn-ssi/assets/includes-cdn/content/alert.html">
                    <noscript>
                        <div class="alert alert-warning" role="alert">
                            <p><i class="fa fa-exclamation-triangle" aria-hidden="true"></i> It appears that JavaScript is disabled or not supported by your browser. To access this service online, please enable JavaScript in your browser settings and try again. Alternatively, you can contact 13 QGOV (13 74 68) for other options to apply.</p>
                        </div>
                    </noscript>

                    <!-- This processes the enclosed content: -->
                    <#nested>

                    <div class="qg-content-footer">
                        <!--
                           <dl>
                            <dt>Last updated:</dt>
                            <dd>22 January 2018</dd>
                            <dt>Last reviewed:</dt>
                            <dd>22 January 2018</dd>
                           </dl>
                        -->
                    </div>
                    <#include "../web-template-release/template-cdn-ssi/assets/includes-cdn/content/options.html">
                </div>

                <#if columns>
                    <aside id="qg-secondary-content">
                        <div class="qg-aside qg-contact" role="complementary">
                            <h2><span class="fa fa-phone fa-2x" aria-hidden="true"></span>Contact</h2>
                            <ul>
                                <li class="telephone">General enquiries <span class="tel">13 QGOV (13 74 68)</span></li>
                            </ul>
                        </div>
                        <#include "../partials/loginLogout.ftlh">
                    </aside>
                </#if>

            </div>

        </div>
    </div>

    <#include "../web-template-release/template-cdn-ssi/assets/includes-cdn/footer/footer-without-location.html">

    <script nonce="${__csp_nonce}" >
        var qg = qg || {};
        qg.swe = qg.swe || {};
        // Used by the feedback form.
        qg.swe.franchiseTitle = 'Franchise Title';
    </script>

    <#include "../web-template-release/template-cdn-ssi/assets/includes-cdn/footer/footer-scripts.html">

    <script nonce="${__csp_nonce}" src="/scripts/autofocus.js"></script>
    <script nonce="${__csp_nonce}" src="/scripts/question-collapse.js"></script>

    <#noautoesc>
        ${footer_script!}
    </#noautoesc>
    <!-- Mopinion Pastea.se  start -->
    <script nonce="${__csp_nonce}" type="text/javascript">(function(){var id="${analyticsMopinion}";var js=document.createElement("script");js.setAttribute("type","text/javascript");js.setAttribute("src","//deploy.mopinion.com/js/pastease.js");js.async=true;document.getElementsByTagName("head")[0].appendChild(js);var t=setInterval(function(){try{new Pastease.load(id);clearInterval(t)}catch(e){}},50)})();</script>
    <!-- Mopinion Pastea.se end -->
    </body>

    </html>
</#macro>
