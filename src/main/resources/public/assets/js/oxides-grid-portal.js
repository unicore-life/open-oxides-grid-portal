var oxidesGridPortalApp = angular.module('oxidesGridPortal', [
    'feeds',
    'angular-spinkit',
    'ngRoute',
    'ui.bootstrap'
]);


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
        $scope.showSpinKit = true;

        $scope.viewFile = function (simulationFile) {
            console.info('Viewing file: ' + simulationFile);

            // TODO
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
                var request = {
                    method: 'GET',
                    url: '/oxides/unicore/jobs/' + absoluteUrl.substr(-36) + '/files',
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
