var oxidesGridPortalApp = angular.module('oxidesGridPortal', [
        'feeds',
        'angular-spinkit',
        'angularFileUpload',
        'ngRoute',
        'ui.bootstrap'
    ]
);

//oxidesGridPortalApp
//   .config(function($routeProvider, $locationProvider) {
//        // use the HTML5 History API
//        $locationProvider.html5Mode(true);
//    });


oxidesGridPortalApp.controller('oxidesSimulationsListingController',
    function ($scope, $location, $http, oxidesSimulationsListingService, modelSimulationsListing) {
        $scope.simulations = modelSimulationsListing;
        $scope.showSpinKit = true;

        $scope.destroyJob = function (uuid, idx) {
            console.warn('Deleting job: ' + uuid + ' (' + idx + ')');
            $scope.simulations.splice(idx, 1);

            $http.delete('/oxides/unicore/jobs/' + uuid, {
                headers: {'Content-Type': 'application/json'},
                data: ''
            })
//            .success(function (data, status, headers, config) {
//            })
                .error(function (data, status, headers, config) {
                    console.error('Failed: HTTP Status Code = ' + status);
                });
        };

        oxidesSimulationsListingService.getJson()
            .success(function (data, status, headers, config) {
                angular.copy(data, modelSimulationsListing);
                $scope.showSpinKit = false;
            })
            .error(function (data, status, headers, config) {
                alert('Failed: HTTP Status Code = ' + status);
                $scope.showSpinKit = false;
            });
    }
);

oxidesGridPortalApp.factory('oxidesSimulationsListingService',
    ['$http', function ($http) {
        return {
            getJson: function () {
                return $http.get('/oxides/unicore/jobs', {
                    headers: {'Content-Type': 'application/json'},
                    data: ''
                });
            }
        };
    }]
);

oxidesGridPortalApp.value('modelSimulationsListing', []);


oxidesGridPortalApp.controller('oxidesSimulationFilesListingController',
    function ($scope, $location, oxidesSimulationFilesListingService) {
        $scope.simulationFiles = [];
        $scope.breadCrumbElements = [];
        $scope.showSpinKit = true;

        $scope.initializeBreadCrumb = function (filePath) {
            var locationUrl = $location.absUrl();
            var i = locationUrl.indexOf('?');
            if (i >= 0) {
                locationUrl = locationUrl.substr(0, i);
            }
            $scope.breadCrumbElements.push({
                label: 'Simulation Directory',
                href: locationUrl
            });

            var pathElements = filePath.split('/');
            var pathSoFar = '/';
            for (var i = 0; i < pathElements.length; i++) {
                if (pathElements[i] != '') {
                    pathSoFar += (pathElements[i] + '/');
                    $scope.breadCrumbElements.push({
                        label: pathElements[i],
                        href: locationUrl + '?path=' + pathSoFar
                    });
                }
            }
            //console.info($scope.breadCrumbElements);
        };

        oxidesSimulationFilesListingService.getSimulationFilesList($location.absUrl())
            .success(function (data, status, headers, config) {
                angular.copy(data, $scope.simulationFiles);
                $scope.showSpinKit = false;
            })
            .error(function (data, status, headers, config) {
                alert('Failed: HTTP Status Code = ' + status);
                $scope.showSpinKit = false;
            });
    }
);

oxidesGridPortalApp.factory('oxidesSimulationFilesListingService',
    ['$http', function ($http) {
        return {
            getSimulationFilesList: function (absoluteUrl) {
                var uuid = "", query = "";
                var queryIndex = absoluteUrl.indexOf("?");

                if (queryIndex >= 0) {
                    uuid = absoluteUrl.substr(0, queryIndex).substr(-36);
                    query = absoluteUrl.substr(queryIndex);
                }
                else {
                    uuid = absoluteUrl.substr(-36);
                }

                var request = {
                    method: 'GET',
                    url: '/oxides/unicore/jobs/' + uuid + '/files' + query,
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    data: ''
                };
                return $http(request);
            }
        };
    }]
);


oxidesGridPortalApp.controller('oxidesSimulationDetailsController',
    function ($scope, $http) {
        $scope.simulationUuid = null;
        $scope.showSpinKit = true;

        $scope.isLogVisible = false;
        $scope.toggleLogLabel = 'Show Log';

        $scope.initialize = function (uuid) {
            $scope.simulationUuid = uuid;
        };

        $scope.toggleLog = function () {
            $scope.isLogVisible = !$scope.isLogVisible;
            $scope.toggleLogLabel = $scope.isLogVisible ? 'Hide Log' : 'Show Log';
        };

        //$http({
        //    method: 'GET',
        //    url: '/oxides/unicore/jobs/' + $scope.simulationUuid + '/details',
        //    headers: {
        //        'Content-Type': 'application/json'
        //    },
        //    data: ''
        //}).then(function (data, status, headers, config) {
        //    $scope.showSpinKit = false;
        //}).catch(function (data, status, headers, config) {
        //    alert('Failed: HTTP Status Code = ' + status);
        //    $scope.showSpinKit = false;
        //});
    }
);


oxidesGridPortalApp.controller('oxidesSubmitSimulationController',
    function ($scope, oxidesSubmitSimulationService, FileUploader) {
        $scope.simulationParameters = {
            simulationName: 'nazwa jako taka',
            simulationProject: 'grancik',
            simulationQueue: '',
            simulationMemory: '',
            simulationNodes: '',
            simulationCPUs: '',
            simulationReservation: ''
        };
        $scope.simulationScriptText = '';
        $scope.simulationUploadFilesList = [];
        $scope.simulationFormFields = [
            {'id': 'simulationName', 'label': 'Name', 'placeholder': 'Simulation Name'},
            {'id': 'simulationProject', 'label': 'Project', 'placeholder': 'Grant ID'},
            {'id': 'simulationQueue', 'label': 'Queue', 'placeholder': 'Queue'},
            {'id': 'simulationMemory', 'label': 'Memory', 'placeholder': 'Memory [MB]'},
            {'id': 'simulationNodes', 'label': 'Nodes Count', 'placeholder': 'Number of nodes'},
            {'id': 'simulationCPUs', 'label': 'CPUs / Node', 'placeholder': 'CPUs per node'},
            {'id': 'simulationReservation', 'label': 'Reservation', 'placeholder': 'Reservation ID'}
        ];

        $scope.visibleAdvanced = false;
        $scope.toggleAdvancedLabel = 'Show Advanced Parameters';


        $scope.toggleAdvanced = function () {
            var prefix = $scope.toggleAdvancedLabel.substr(0, 4).toLowerCase();
            var toggledPrefix = (prefix == 'show') ? 'Hide' : 'Show';

            $scope.toggleAdvancedLabel = toggledPrefix + ' Advanced Parameters';
            $scope.visibleAdvanced = !$scope.visibleAdvanced;
        };

        $scope.isParameterRequired = function (parameterName) {
            if (parameterName == 'simulationName' || parameterName == 'simulationProject') {
                return true;
            }
            return false;
        };
        $scope.isAdvancedVisible = function (parameterName) {
            return $scope.isParameterRequired(parameterName) || $scope.visibleAdvanced;
        };


        $scope.resetForm = function () {
            $scope.simulationSubmitForm.$setPristine();
        };

        $scope.submitForm = function () {
            // Creating JSON with simulation description:
            var oxidesSimulation = {};
            var offset = 'simulation'.length;
            $scope.simulationFormFields.map(function (it) {
                var parameterValue = $scope.simulationParameters[it.id];
                if (parameterValue != null && parameterValue != '') {
                    var parameterName = it.id.substr(offset).toLowerCase();
                    oxidesSimulation[parameterName] = parameterValue;
                }
            });
            oxidesSimulation['script'] = $scope.simulationScriptText;
            oxidesSimulation['files'] = $scope.simulationUploadFilesList;
            var oxidesSimulationJson = JSON.stringify(oxidesSimulation);

            // Submitting simulation:
            oxidesSubmitSimulationService.submitSimulation(oxidesSimulationJson)
                .success(function (data, status, headers, config) {
                    console.info('Submitted: HTTP Status Code = ' + status);
                })
                .error(function (data, status, headers, config) {
                    console.error('Failed: HTTP Status Code = ' + status);
                });
        };


        $scope.removeUploadFile = function (idx) {
            $scope.simulationUploadFilesList.splice(idx, 1);
        };

        $scope.uploader = new FileUploader({
            url: '/oxides/unicore/upload',
            alias: 'uploadFile',
            queueLimit: 1,
            //        formData: [{
            //            destinationUri: $scope.destinationUri
            //        }],
            removeAfterUpload: true,

//            onBeforeUploadItem: function (item) {
//                Array.prototype.push.apply(item.formData, [{
//                    destinationUri: $scope.destinationUri,
//                }]);
//            },

            onSuccessItem: function (item, response, status, headers) {
                console.info('SUCCESS');
            },

            onCompleteItem: function (item, response, status, headers) {
                this.clearQueue();

                var filename = item.file.name;
                var arrayLength = $scope.simulationUploadFilesList.length;
                for (var i = arrayLength - 1; i >= 0; i--) {
                    if ($scope.simulationUploadFilesList[i] === filename) {
                        $scope.simulationUploadFilesList.splice(i, 1);
                    }
                }
                $scope.simulationUploadFilesList.push(filename);

                // Workaround for re-upload:
                $scope.uploader._directives.select[0].element[0].value = '';
                console.log('STATUS: ' + status);
            },
        });
    }
);

oxidesGridPortalApp.factory('oxidesSubmitSimulationService',
    ['$http', function ($http) {
        return {
            submitSimulation: function (oxidesSimulationJson) {
                console.info(oxidesSimulationJson);

                var request = {
                    method: 'POST',
                    url: '/oxides/unicore/submit',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    data: oxidesSimulationJson
                };
                return $http(request);
            }
        };
    }]
);


/**
 * A generic confirmation for risky actions.
 * Usage: Add attributes: ng-really-message="Are you sure"? ng-really-click="takeAction()" function
 */
oxidesGridPortalApp.directive('ngReallyClick',
    [function () {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                element.bind('click', function () {
                    var message = attrs.ngReallyMessage;
                    if (message && confirm(message)) {
                        scope.$apply(attrs.ngReallyClick);
                    }
                });
            }
        }
    }]
);
