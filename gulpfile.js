var watchify = require('watchify');
var browserify = require('browserify');
var gulp = require('gulp');
var source = require('vinyl-source-stream');
var buffer = require('vinyl-buffer');
var gutil = require('gulp-util');
var gulpif = require('gulp-if');
var assign = require('lodash.assign');
var less = require('gulp-less');
var minifyCSS = require('gulp-minify-css');
var uglify = require('gulp-uglify');

var argv = require('yargs').argv;

// add custom browserify options here
var customOpts = {
  entries: ['./src/app.js'],
  debug: !argv.prod
};
var opts = assign({}, watchify.args, customOpts);
var jsPipe = watchify(browserify(opts));

gulp.task('watch-js', bundleJS);

gulp.task('build-js', function () {
    jsPipe = browserify(opts);
    return bundleJS(browserify(opts));
});

gulp.task('build-css', function () {
    return gulp.src("./style.less")
        .pipe(less())
        .pipe(gulpif(argv.prod, minifyCSS({ keepBreaks: true })))
        .pipe(gulp.dest("."));
});

gulp.task('watch-css', function () {
    gulp.watch("./style.less", ['build-css']);
});

gulp.task('watch', ['build-css', 'watch-js', 'watch-css']);
gulp.task('build-prod', ['build-css', 'build-js']);
jsPipe.on('update', bundleJS); // on any dep update, runs the bundler
jsPipe.on('log', gutil.log); // output build logs to terminal

function bundleJS() {
  return jsPipe
    .transform('reactify', { es6: true, target: "es5" })
    .bundle()
    // log errors if they happen
    .on('error', gutil.log.bind(gutil, 'Browserify Error'))
    .pipe(source('wikizen.js'))
    .pipe(buffer())
    .pipe(gulpif(argv.prod, uglify()))
    .pipe(gulp.dest('.'));
}