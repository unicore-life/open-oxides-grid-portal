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

//.config(['$locationProvider', function($locationProvider) {
//        $locationProvider.html5Mode(true);
//    }]
//);


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
    function ($scope, $location, $routeParams, oxidesSimulationFilesListingService) {
        $scope.simulationFiles = [];
        $scope.filePath;
        $scope.showSpinKit = true;

        $scope.viewFile = function (simulationFile) {
            console.info('Viewing file: ' + simulationFile + ' / ' + $scope.filePath);

            // TODO
        };

        $scope.getBreadCrumbPath = function () {
            console.log($scope.filePath);
            var result = [ ];

            var pathElements = $scope.filePath.replace(/\/$/, "").split('/');
            console.log("pathElements = {" + pathElements + "}");

            var arrayLength = pathElements.length;
            for (var i = 1; i < arrayLength; i++) {
                result.push(pathElements[i]);
            }
            result[0] = 'Simulation Directory';

            console.info(result);
            return result;
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


oxidesGridPortalApp.controller('oxidesSubmitSimulationController', function ($scope) {
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
        console.log($scope.simulationSubmitForm);
    }
});
