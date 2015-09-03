var oxidesGridPortalApp = angular.module('oxidesGridPortal', [
        'feeds',
        'angular-spinkit',
        'ngRoute',
        'ui.bootstrap'
    ]
);

//oxidesGridPortalApp
//   .config(function($routeProvider, $locationProvider) {
//        // use the HTML5 History API
//        $locationProvider.html5Mode(true);
//    });


oxidesGridPortalApp.controller('oxidesGridPortalController', function ($scope) {
    $scope.phones = [
        {
            'name': 'Nexus S',
            'snippet': 'Fast just got faster with Nexus S.'
        },
        {
            'name': 'Motorola XOOM� with Wi-Fi',
            'snippet': 'The Next, Next Generation tablet.'
        },
        {
            'name': 'MOTOROLA XOOM�',
            'snippet': 'The Next, Next Generation tablet.'
        }
    ];

    $scope.yourName = null;
});


oxidesGridPortalApp.controller('oxidesSimulationsListingController',
    function ($scope, $location, $http, oxidesSimulationsListingService, modelSimulationsListing) {
        $scope.simulations = modelSimulationsListing;
        $scope.showSpinKit = true;

        $scope.destroyJob = function (uuid) {
            //$http.delete('/oxides/unicore/jobs')
            console.warn('Deleting job: ' + uuid);

            // TODO
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


oxidesGridPortalApp.controller('oxidesSubmitSimulationController',
    function ($scope, oxidesSubmitSimulationService) {
        $scope.simulationParameters = {
            simulationName: 'A',
            simulationProject: 'B',
            simulationQueue: 'c',
            simulationMemory: 'd',
            simulationNodes: 'e',
            simulationCPUs: 'f'
        };
        $scope.simulationFormFields = [
            {'id': 'simulationName', 'label': 'Name', 'placeholder': 'Simulation Name'},
            {'id': 'simulationProject', 'label': 'Project', 'placeholder': 'Grant ID'},
            {'id': 'simulationQueue', 'label': 'Queue', 'placeholder': 'Queue'},
            {'id': 'simulationMemory', 'label': 'Memory', 'placeholder': 'Memory [MB]'},
            {'id': 'simulationNodes', 'label': 'Nodes Count', 'placeholder': 'Number of nodes'},
            {'id': 'simulationCPUs', 'label': 'CPUs / Node', 'placeholder': 'CPUs per node'}
        ];

        $scope.resetForm = function () {
            $scope.simulationSubmitForm.$setPristine();
        };

        $scope.submitForm = function () {
            // Creating JSON with simulation description:
            var oxidesSimulation = {};
            var offset = 'simulation'.length;
            $scope.simulationFormFields.map(function(it) {
                var parameterValue = $scope.simulationParameters[it.id];
                if (parameterValue != null && parameterValue != '') {
                    var parameterName = it.id.substr(offset).toLowerCase();
                    oxidesSimulation[parameterName] = parameterValue;
                }
            });
            var oxidesSimulationJson = JSON.stringify(oxidesSimulation);

            // Submitting simulation:
            oxidesSubmitSimulationService.submitSimulation(oxidesSimulationJson)
                .success(function (data, status, headers, config) {
                    console.info('Submitted: HTTP Status Code = ' + status);
                })
                .error(function (data, status, headers, config) {
                    console.error('Failed: HTTP Status Code = ' + status);
                });
        }
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

