angular
    .module('oxidesGridPortal')
    .controller('oxidesSimulationFilesListingController',

    ['$scope', '$location',
        'oxidesSimulationFilesListingService',
        'oxidesSimulationFilePathBreadCrumbService',
        'oxidesPopMessageHandlingService',

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
