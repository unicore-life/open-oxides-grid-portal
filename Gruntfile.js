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
                'src/web/js/**/*.js',
                'src/web/spec/**/*.js'
            ],
            options: {
                jshintrc: 'config/.jshintrc'
            }
        },
        concat: {
            js: {
                src: [
                    'bower_components/jquery/jquery.js',
                    'bower_components/bootstrap/dist/js/bootstrap.js',
                    'bower_components/angular/angular.js',
                    'bower_components/angular-animate/angular-animate.js',
                    'bower_components/angular-route/angular-route.js',
                    'bower_components/angular-feeds/app/angular-feeds/angular-feeds.js',
                    'bower_components/angularjs-toaster/toaster.js',
                    'bower_components/angular-bootstrap/ui-bootstrap.js',
                    'bower_components/angular-file-upload/dist/angular-file-upload.min.js',
                    'bower_components/angular-spinkit/build/angular-spinkit.js',
                    'src/web/js/oxides-grid-portal.js',
                    'src/web/js/*/**/*.js'
                ],
                dest: 'src/main/resources/public/assets/open-oxides-grid-portal.js'
            }
        },
        uglify: {
            js: {
                files: {
                    'src/main/resources/public/assets/open-oxides-grid-portal.min.js': [
                        'src/main/resources/public/assets/open-oxides-grid-portal.js'
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
                    'src/main/resources/public/assets/open-oxides-grid-portal.css': 'src/web/less/open-oxides-grid-portal.less'
                }
            }
        },
        clean: {
            'bootstrap-fonts': ['src/main/resources/public/fonts/'],
            'build': [
                'src/main/resources/public/assets/open-oxides-grid-portal.css',
                'src/main/resources/public/assets/open-oxides-grid-portal.js',
                'src/main/resources/public/assets/open-oxides-grid-portal.min.js',
                'src/main/resources/public/assets/open-oxides-grid-portal.min.js.map'
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
