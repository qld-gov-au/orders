<#macro page columns=true admin=false fullscreen=false hidetitle=false className="">
    <!DOCTYPE html>
    <html lang="en">
    <head>

        <#assign qgov_assets_url="https://static.qgov.net.au/assets/v2" />

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

        <#include "../web-template-release/head-assets.html"/>

        <#noautoesc>
            ${head!}
        </#noautoesc>
        <script nonce="${__csp_nonce}" src="${papiBase}/minicart/contents_1.0.js" type="text/javascript"></script>
        <link nonce="${__csp_nonce}" type="text/css" href="${papiBase}/ui/minicart_1.0.css" rel="stylesheet" />

        <noscript>
            <style>
                .collapse{
                    display: block !important;
                }
            </style>
        </noscript>

    </head>

    <body data-qg-accessibility="true">

    <#include "../web-template-release/analytics.html">
    <#include "../web-template-release/header/access.html">

    <#include "../web-template-release/header/header-without-location.html">

    <#noautoesc>
        ${breadcrumb!} <#-- or include default <#include "../partials/breadcrumbs.ftlh"> -->
    </#noautoesc>

    <#--
    <#include "../web-template-release/breadcrumbs/breadcrumb-demo.html">
    -->

    <div class="container-fluid qg-site-width">


        <div id="qg-content">

            <div id="<#if columns>qg-three-col<#else>qg-two-col-aside</#if>" class="row wide">

                <!-- left-hand column -->
                <#noautoesc>
                    ${leftHandNav!} <#-- or include default <#include "../partials/left-hand-nav.ftlh"> -->
                </#noautoesc>

                <div id="qg-primary-content" role="main">

                    <#include "../web-template-release/content/alert.html">
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
                    <#include "../web-template-release/content/options.html">
                </div>

                <#if columns>
                    <aside id="qg-secondary-content">

                        <div class="qg-aside" id="minicart" >
                            <div class="inner">
                                <div id="ssq-minicart" class="placeholder">
                                    <h2>Cart</h2>
                                    <div id="ssq-minicart-view">
                                        <div class="ssq-minicart-loading"><p>Loading <a href="${papiBase}/cart/view">cart</a>...</p></div>
                                        <noscript>
                                            <p class="ssq-minicart-noscript">Edit cart or checkout to place your order.</p>
                                            <div class="ssq-minicart-submit">
                                                <input type="hidden" id="ssq-cart-contents" name="ssq-cart-contents" value="" />
                                                <img src="${papiBase}/minicart/synchronise?cartId=(cartId)" id="ssq-synch-img" height="0" width="0" alt="" />
                                                <a href="${papiBase}/cart/checkout" id="ssq-cart-checkout">
                                                    <img id="ssq_minicart_checkout" src="${papiBase}/minicart/btn-checkout.png" alt="Checkout" />
                                                </a>
                                                <a href="${papiBase}/cart/view" id="ssq-cart-edit">
                                                    <img id="ssq_minicart_cart" src="${papiBase}/minicart/btn-cart.png" alt="Edit cart" />
                                                </a>
                                            </div>
                                        </noscript>

                                    </div>
                                    <#if cartId?has_content>
                                        <script nonce="${__csp_nonce}" type="text/javascript">
                                            sessionStorage.setItem("cartId", ${cartId})
                                        </script>
                                    </#if>
                                    <div class="ssq-minicart-cards">
                                        <h3>Cards accepted</h3>
                                        <ul>
                                            <li>
                                                <img src="${papiBase}/minicart/visa.png" alt="Visa"/>
                                            </li>
                                            <li>
                                                <img src="${papiBase}/minicart/mastercard.png" alt="MasterCard" />
                                            </li>
                                        </ul>
                                    </div>

                                </div>
                            </div>
                        </div>

                        <div class="qg-aside qg-contact" role="complementary">
                            <h2><span class="fa fa-phone fa-2x" aria-hidden="true"></span>Contact</h2>
                            <ul>
                                <li class="telephone">General enquiries <span class="tel">13 QGOV (13 74 68)</span></li>
                            </ul>
                        </div>
<#--                        <#include "../partials/loginLogout.ftlh">-->
                    </aside>
                </#if>

            </div>

        </div>
    </div>

    <#include "../web-template-release/footer/footer-without-location.html">

    <script nonce="${__csp_nonce}" >
        var qg = qg || {};
        qg.swe = qg.swe || {};
        // Used by the feedback form.
        qg.swe.franchiseTitle = 'Franchise Title';
    </script>

    <#include "../web-template-release/footer/footer-scripts.html">

    <script nonce="${__csp_nonce}" src="${papiBase}/ui/minicart_2.0.js" type="text/javascript"></script>
    <#noautoesc>
        ${footer_script!}
    </#noautoesc>
    </body>

    </html>
</#macro>
