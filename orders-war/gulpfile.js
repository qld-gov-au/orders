'use strict';

var gulp = require('gulp');
var path = require('path');
var replace = require('gulp-replace');
var filter = require('gulp-filter');


function webTemplateReleaseInstall () {
    var htmlFilter = filter('**/*.html', {restore: true});
    return gulp.src([
        path.resolve('./node_modules/web-template-release/template-cdn-ssi/**/*'),
        path.resolve('./node_modules/web-template-release/package.json')
    ], {base:"./node_modules/web-template-release/template-cdn-ssi/assets/includes-cdn"})
        // See http://mdn.io/string.replace#Specifying_a_string_as_a_parameter
        .pipe(replace(/(GTM[A-Za-z0-9-]+)/g, '${analyticsGtmKey}'))
        .pipe(replace(/<!--#include virtual="\/assets\/includes-cdn\/([a-zA-Z-]+)\/(.*)"-->/g, '<#include "..\/$1\/$2"\/>'))
        .pipe(replace(/<!--#include virtual="(.*)"-->/g, '<#include "$1"/>'))
        .pipe(replace(/(test-static|static)\.qgov\.net\.au/g, '${cdnEnvironment}'))
        .pipe(htmlFilter).pipe(replace(/(?<!<!--|<noscript>\n?\s*)<(script|link|style)/g, '<$1 nonce="${__csp_nonce}"'))
        .pipe(htmlFilter.restore)
        .pipe(gulp.dest('./target/generated-sources/src/main/resources/templates/web-template-release'));
}
exports.webTemplateReleaseInstall = webTemplateReleaseInstall;

exports.build = gulp.series(webTemplateReleaseInstall);

exports.default = gulp.series(webTemplateReleaseInstall);
