var oxidesGridPortalApp = angular.module('oxidesGridPortal');


oxidesGridPortalApp.factory('oxidesPopMessageHandlingService',
    ['toaster', function (toaster) {
        return {
            handleError: function (response) {
                var toasterMessage = response.statusText;
                if (response.status === 0 && toasterMessage === '') {
                    toasterMessage = 'No connection or timeout/cancel occurred.';
                }
                if (response.data) {
                    toasterMessage += '. ' + (response.data.message || '');
                }

                toaster.pop('error', 'Request failed (' + response.status + ')', toasterMessage);

                console.error('Failed: HTTP Status Code = ' + response.status);
                //console.log(response);
                //alert('Failed: HTTP Status Code = ' + response.status);
            },

            handleDeletionSuccess: function (response, uuid, idx) {
                console.info('Deleting simulation ' + uuid + ' (' + idx + ') accepted.');
            },

            handleSubmissionSuccess: function (response, oxidesSimulation) {
                toaster.pop('info', 'Simulation submitted (' + response.status + ')',
                    'Simulation "' + oxidesSimulation.name + '" sent to UNICORE broker.');

                console.info('Submitted: HTTP Status Code = ' + response.status);
                //console.log(response);
            }
        };
    }]
);


oxidesGridPortalApp.controller('oxidesSimulationsListingController',
    ['$scope', '$location', '$http', 'oxidesSimulationsListingService', 'modelSimulationsListing', 'oxidesPopMessageHandlingService',
        function ($scope, $location, $http,
                  oxidesSimulationsListingService,
                  modelSimulationsListing,
                  oxidesPopMessageHandlingService) {
            $scope.simulations = modelSimulationsListing;
            $scope.showSpinKit = true;

            $scope.initialize = function () {
                oxidesSimulationsListingService.getJson()
                    .then(function (response) {
                        angular.copy(response.data, modelSimulationsListing);
                        $scope.showSpinKit = false;
                    })
                    .catch(function (response) {
                        oxidesPopMessageHandlingService.handleError(response);
                        $scope.showSpinKit = false;
                    });
            };

            $scope.destroyJob = function (uuid, idx) {
                $http.delete('/unicore/jobs/' + uuid, {
                    headers: {'Content-Type': 'application/json'},
                    data: ''
                })
                    .then(function (response) {
                        oxidesPopMessageHandlingService.handleDeletionSuccess(response, uuid, idx);
                        $scope.simulations.splice(idx, 1);
                    })
                    .catch(function (response) {
                        oxidesPopMessageHandlingService.handleError(response);
                    });
            };

            $scope.refreshSimulationList = function () {
                $scope.showSpinKit = true;
                $scope.initialize();
            };
        }
    ]
);

oxidesGridPortalApp.factory('oxidesSimulationsListingService',
    ['$http', function ($http) {
        return {
            getJson: function () {
                return $http.get('/unicore/jobs', {
                    headers: {'Content-Type': 'application/json'},
                    data: ''
                });
            }
        };
    }]
);

oxidesGridPortalApp.value('modelSimulationsListing', []);


oxidesGridPortalApp.factory('oxidesSimulationFilePathBreadCrumbService',
    [function () {
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
                    if (pathElements[i] !== '') {
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
    }]
);

oxidesGridPortalApp.controller('oxidesSimulationFilesListingController',
    ['$scope', '$location', 'oxidesSimulationFilesListingService', 'oxidesSimulationFilePathBreadCrumbService', 'oxidesPopMessageHandlingService',
        function ($scope, $location,
                  oxidesSimulationFilesListingService,
                  oxidesSimulationFilePathBreadCrumbService,
                  oxidesPopMessageHandlingService) {
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
                        oxidesPopMessageHandlingService.handleError(response);
                        $scope.showSpinKit = false;
                    });
            };

            $scope.hasJsMolExtension = function (simulationFile) {
                return simulationFile.type === 'jsmol'
                    || simulationFile.type === 'jmol'
                    || simulationFile.type === 'mol';
            };
        }
    ]
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
                    url: '/unicore/jobs/' + simulationUuid + '/files' + query,
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
    ['$scope', function ($scope) {
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
    }]
);


oxidesGridPortalApp.controller('oxidesMoleculeViewerController',
    ['$scope', '$location', 'oxidesSimulationFilePathBreadCrumbService',
        function ($scope, $location, oxidesSimulationFilePathBreadCrumbService) {
            $scope.breadCrumbElements = [];

            $scope.initialize = function (uuid, path) {
                var locationUrl = '/simulations/' + uuid + '/files';
                angular.copy(
                    oxidesSimulationFilePathBreadCrumbService
                        .getBreadCrumbElementsList(path, locationUrl),
                    $scope.breadCrumbElements);
            };
        }
    ]
);


oxidesGridPortalApp.controller('oxidesSubmitSimulationController',
    ['$scope', 'oxidesSubmitSimulationService', 'FileUploader', 'oxidesPopMessageHandlingService',
        function ($scope, oxidesSubmitSimulationService, FileUploader, oxidesPopMessageHandlingService) {
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
                var toggledPrefix = (prefix === 'show') ? 'Hide' : 'Show';

                $scope.toggleAdvancedLabel = toggledPrefix + ' Advanced Parameters';
                $scope.visibleAdvanced = !$scope.visibleAdvanced;
            };

            $scope.isParameterRequired = function (parameterName) {
                if (parameterName === 'simulationName') {
                    return true;
                }
                return false;
            };
            $scope.isAdvancedVisible = function (parameterName) {
                if (parameterName === 'simulationProject') {
                    return true;
                }
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
                    if (parameterValue !== null && parameterValue !== '') {
                        var parameterName = it.id.substr(offset).toLowerCase();
                        oxidesSimulation[parameterName] = parameterValue;
                    }
                });
                oxidesSimulation.script = $scope.simulationScriptText;
                oxidesSimulation.files = $scope.simulationUploadFilesList;
                var oxidesSimulationJson = JSON.stringify(oxidesSimulation);

                // Submitting simulation:
                oxidesSubmitSimulationService
                    .submitSimulation(oxidesSimulationJson)
                    .then(function (response) {
                        oxidesPopMessageHandlingService.handleSubmissionSuccess(response, oxidesSimulation);
                        $scope.isSubmitting = false;
                    })
                    .catch(function (response) {
                        oxidesPopMessageHandlingService.handleError(response);
                        $scope.isSubmitting = false;
                    });
            };


            $scope.removeUploadFile = function (idx) {
                $scope.simulationUploadFilesList.splice(idx, 1);
            };

            $scope.uploader = new FileUploader({
                url: '/unicore/upload',
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
    ]
);

oxidesGridPortalApp.factory('oxidesSubmitSimulationService',
    ['$http', function ($http) {
        return {
            submitSimulation: function (oxidesSimulationJson) {
                var request = {
                    method: 'POST',
                    url: '/unicore/submit',
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
