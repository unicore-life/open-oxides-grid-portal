var oxidesGridPortalApp = angular.module('oxidesGridPortal', [
        'feeds',
        'toaster',
        'angular-spinkit',
        'angularFileUpload',
        'ngAnimate',
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
    function ($scope, $location, $http, toaster, oxidesSimulationsListingService, modelSimulationsListing) {
        $scope.simulations = modelSimulationsListing;
        $scope.showSpinKit = true;

        $scope.initialize = function () {
            oxidesSimulationsListingService.getJson()
                .then(function (response) {
                    angular.copy(response.data, modelSimulationsListing);
                    $scope.showSpinKit = false;
                })
                .catch(function (response) {
                    // TODO: handling errors
                    toaster.pop({
                        type: 'error',
                        title: 'Connection failed',
                        body: 'Failed: HTTP Status Code = ' + response.status,
                        showCloseButton: true
                    });

                    //alert('Failed: HTTP Status Code = ' + response.status);
                    $scope.showSpinKit = false;
                });
        };

        $scope.destroyJob = function (uuid, idx) {
            console.warn('Deleting job: ' + uuid + ' (' + idx + ')');
            $scope.simulations.splice(idx, 1);

            $http.delete('/oxides/unicore/jobs/' + uuid, {
                headers: {'Content-Type': 'application/json'},
                data: ''
            })
                .catch(function (response) {
                    // TODO: handling errors
                    console.error('Failed: HTTP Status Code = ' + response.status);
                });
        };

        $scope.refreshSimulationList = function () {
            $scope.showSpinKit = true;
            $scope.initialize();
        };
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


oxidesGridPortalApp.factory('oxidesSimulationFilePathBreadCrumbService',
    function () {
        return {
            getBreadCrumbElementsList: function (path, locationUrl) {
                var breadCrumbElements = [];

                breadCrumbElements.push({
                    label: 'Simulation Directory',
                    href: locationUrl
                });

                var pathElements = decodeURIComponent(path).split('/');
                var pathSoFar = '/';
                for (var i = 0; i < pathElements.length; i++) {
                    if (pathElements[i] != '') {
                        pathSoFar += (pathElements[i] + '/');
                        breadCrumbElements.push({
                            label: pathElements[i],
                            href: locationUrl + '?path=' + pathSoFar
                        });
                    }
                }
                return breadCrumbElements;
            }
        };
    }
);

oxidesGridPortalApp.controller('oxidesSimulationFilesListingController',
    function ($scope, $location, oxidesSimulationFilesListingService, oxidesSimulationFilePathBreadCrumbService) {
        $scope.simulationUuid = null;
        $scope.simulationFiles = [];
        $scope.breadCrumbElements = [];
        $scope.showSpinKit = true;

        $scope.initializeBreadCrumb = function (uuid, path) {
            $scope.simulationUuid = uuid;

            var locationUrl = $location.absUrl();
            var i = locationUrl.indexOf('?');
            if (i >= 0) {
                locationUrl = locationUrl.substr(0, i);
            }
            angular.copy(
                oxidesSimulationFilePathBreadCrumbService
                    .getBreadCrumbElementsList(path, locationUrl),
                $scope.breadCrumbElements);

            oxidesSimulationFilesListingService
                .getSimulationFilesList($scope.simulationUuid, $location.absUrl())
                .then(function (response) {
                    angular.copy(response.data, $scope.simulationFiles);
                    $scope.showSpinKit = false;
                })
                .catch(function (response) {
                    // TODO: handling errors
                    alert('Failed: HTTP Status Code = ' + response.status);
                    $scope.showSpinKit = false;
                });
        };

        $scope.hasJsMolExtension = function (simulationFile) {
            return simulationFile.type == 'jsmol'
                || simulationFile.type == 'jmol'
                || simulationFile.type == 'mol';
        };
    }
);

oxidesGridPortalApp.factory('oxidesSimulationFilesListingService',
    ['$http', function ($http) {
        return {
            getSimulationFilesList: function (simulationUuid, absoluteUrl) {
                var query = "";
                var queryIndex = absoluteUrl.indexOf("?");
                if (queryIndex >= 0) {
                    query = absoluteUrl.substr(queryIndex);
                }

                var request = {
                    method: 'GET',
                    url: '/oxides/unicore/jobs/' + simulationUuid + '/files' + query,
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
    }
);


oxidesGridPortalApp.controller('oxidesMoleculeViewerController',
    function ($scope, $location, oxidesSimulationFilePathBreadCrumbService) {
        $scope.breadCrumbElements = [];

        $scope.initialize = function (uuid, path) {
            var locationUrl = '/oxides/simulations/' + uuid + '/files';
            angular.copy(
                oxidesSimulationFilePathBreadCrumbService
                    .getBreadCrumbElementsList(path, locationUrl),
                $scope.breadCrumbElements);
        };
    }
);


oxidesGridPortalApp.controller('oxidesSubmitSimulationController',
    function ($scope, oxidesSubmitSimulationService, FileUploader) {
        $scope.simulationParameters = {
            simulationName: '',
            simulationProject: '',
            simulationQueue: '',
            simulationMemory: '',
            simulationNodes: '',
            simulationCPUs: '',
            simulationReservation: '',
            simulationProperty: ''
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
            {'id': 'simulationReservation', 'label': 'Reservation', 'placeholder': 'Reservation ID'},
            {'id': 'simulationProperty', 'label': 'Nodes Property', 'placeholder': 'Nodes property string'}
        ];

        $scope.isSubmitting = undefined;
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
            $scope.isSubmitting = true;

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
            oxidesSubmitSimulationService
                .submitSimulation(oxidesSimulationJson)
                .then(function (response) {
                    // TODO: handling success

                    console.info('Submitted: HTTP Status Code = ' + response.status);
                    $scope.isSubmitting = false;
                })
                .catch(function (response) {
                    // TODO: handling errors

                    console.error('Failed: HTTP Status Code = ' + response.status);
                    $scope.isSubmitting = false;
                });
        };


        $scope.removeUploadFile = function (idx) {
            $scope.simulationUploadFilesList.splice(idx, 1);
        };

        $scope.uploader = new FileUploader({
            url: '/oxides/unicore/upload',
            alias: 'uploadFile',
            queueLimit: 1,
            autoUpload: true,
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
                var filename = item.file.name;
                var arrayLength = $scope.simulationUploadFilesList.length;
                for (var i = arrayLength - 1; i >= 0; i--) {
                    if ($scope.simulationUploadFilesList[i] === filename) {
                        $scope.simulationUploadFilesList.splice(i, 1);
                    }
                }
                $scope.simulationUploadFilesList.push(filename);
            },

            onCompleteItem: function (item, response, status, headers) {
                this.clearQueue();

                // Workaround for re-upload:
                $scope.uploader._directives.select[0].element[0].value = '';
            }
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
