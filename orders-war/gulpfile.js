'use strict';

var gulp = require('gulp');
var path = require('path');
var replace = require('gulp-replace');
var del = require('del');

gulp.task('web-template-release-Clean', function () {
    return del(['./target/generated-sources/src/main/resources/templates/web-template-release/**'], {force:true});
});

gulp.task('web-template-release-Install', function(){
    return gulp.src([
        path.resolve('./node_modules/web-template-release/template-cdn-ssi/assets/**/*'),
        path.resolve('./node_modules/web-template-release/package.json')
    ], {base:"./node_modules/web-template-release/template-cdn-ssi/assets/includes-cdn"})
        // See http://mdn.io/string.replace#Specifying_a_string_as_a_parameter
        .pipe(replace(/GTM-[A-Za-z0-9]{6,10}/g, '${analyticsGtmKey}'))
        .pipe(replace(/<!--#include virtual="\/assets\/includes-cdn\/([a-zA-Z-]+)\/(.*)"-->/g, '<#include "..\/$1\/$2"\/>'))
        .pipe(replace(/<!--#include virtual="(.*)"-->/g, '<#include "$1"/>'))
        .pipe(replace(/(test-static|static)\.qgov\.net\.au/g, "${cdnEnvironment}"))
        .pipe(replace(/<script( )src/g, "<script nonce=\"${__csp_nonce}\" src"))
        .pipe(replace(/<link( )href/g, "<link nonce=\"${__csp_nonce}\" href"))
        .pipe(replace(/<script>/g, "<script nonce=\"${__csp_nonce}\">"))
        .pipe(gulp.dest('./target/generated-sources/src/main/resources/templates/web-template-release'));
});

gulp.task('build', gulp.series('web-template-release-Clean', 'web-template-release-Install'));

exports.default = gulp.task('build');