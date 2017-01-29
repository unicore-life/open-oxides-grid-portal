module.exports = function (grunt) {
    require('load-grunt-tasks')(grunt);

    grunt.initConfig({
        'bower-install-simple': {
            prod: {
                options: {
                    production: true
                }
            }
        },
        jshint: {
            all: [
                'Gruntfile.js',
                'src/web/javascript/**/*.js',
                'src/web/spec/**/*.js'
            ],
            options: {
                jshintrc: 'config/.jshintrc'
            }
        },
        concat: {
            js: {
                files: {
                    'src/main/resources/public/assets/oxides-grid-portal-dependencies.js': [
                        'bower_components/jquery/dist/jquery.js',
                        'bower_components/bootstrap/dist/js/bootstrap.js',
                        'bower_components/yui/build/yui/yui.js',
                        'bower_components/yui/build/oop/oop.js',
                        'bower_components/yui/build/jsonp/jsonp.js',
                        'bower_components/yui/build/jsonp-url/jsonp-url.js',
                        'bower_components/yui/build/yql/yql.js',
                        'bower_components/yui/build/yql-jsonp/yql-jsonp.js',
                        'bower_components/angular/angular.js',
                        'bower_components/angular-animate/angular-animate.js',
                        'bower_components/angular-route/angular-route.js',
                        'src/web/angular-feeds-simplified-for-json.js',
                        'bower_components/angularjs-toaster/toaster.js',
                        'bower_components/angular-bootstrap/ui-bootstrap.js',
                        'bower_components/angular-file-upload/dist/angular-file-upload.js',
                        'bower_components/angular-spinkit/build/angular-spinkit.js'
                    ],
                    'src/main/resources/public/assets/oxides-grid-portal-main.js': [
                        'src/web/javascript/oxides-grid-portal.js',
                        'src/web/javascript/*/**/*.js'
                    ],
                    'src/main/resources/public/assets/oxides-grid-portal-full.js': [
                        'src/main/resources/public/assets/oxides-grid-portal-dependencies.js',
                        'src/main/resources/public/assets/oxides-grid-portal-main.js'
                    ]
                }
            }
        },
        uglify: {
            js: {
                files: {
                    'src/main/resources/public/assets/oxides-grid-portal-full.min.js': [
                        'src/main/resources/public/assets/oxides-grid-portal-full.js'
                    ]
                },
                options: {
                    sourceMap: true
                }
            }
        },
        copy: {
            'bootstrap-fonts': {
                files: [
                    {
                        'expand': true,
                        'cwd': 'bower_components/bootstrap/dist/fonts/',
                        'src': ['*'],
                        'dest': 'src/main/resources/public/fonts/',
                        'filter': 'isFile'
                    }
                ]
            }
        },
        less: {
            prod: {
                options: {
                    compress: true,
                    optimization: 2,
                    paths: 'src/web/less/'
                },
                files: {
                    'src/main/resources/public/assets/oxides-grid-portal.css': 'src/web/less/oxides-grid-portal.less'
                }
            }
        },
        clean: {
            'bootstrap-fonts': ['src/main/resources/public/fonts/'],
            'build': [
                'src/main/resources/public/assets/oxides-grid-portal.css',
                'src/main/resources/public/assets/oxides-grid-portal-dependencies.js',
                'src/main/resources/public/assets/oxides-grid-portal-dependencies.min.js',
                'src/main/resources/public/assets/oxides-grid-portal-dependencies.min.js.map',
                'src/main/resources/public/assets/oxides-grid-portal-main.js',
                'src/main/resources/public/assets/oxides-grid-portal-main.min.js',
                'src/main/resources/public/assets/oxides-grid-portal-main.min.js.map',
                'src/main/resources/public/assets/oxides-grid-portal-full.js',
                'src/main/resources/public/assets/oxides-grid-portal-full.min.js',
                'src/main/resources/public/assets/oxides-grid-portal-full.min.js.map'
            ]
        }
    });

    grunt.task.registerTask('install', [
        'bower-install-simple'
    ]);
    grunt.task.registerTask('check', [
        'jshint'
    ]);
    grunt.task.registerTask('build', [
        'jshint',
        'clean',
        'concat:js',
        'uglify:js',
        'less',
        'copy'
    ]);
    grunt.registerTask('default', []);
};
