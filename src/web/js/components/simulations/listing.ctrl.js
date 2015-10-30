var oxidesGridPortalApp = angular.module('oxidesGridPortal');

oxidesGridPortalApp.controller('oxidesSimulationsListingController',
    ['$scope', '$location', '$http', 'oxidesSimulationsListingService', 'modelSimulationsListing', 'oxidesPopMessageHandlingService',
        function ($scope, $location, $http,
                  oxidesSimulationsListingService,
                  modelSimulationsListing,
                  oxidesPopMessageHandlingService) {
            $scope.simulations = modelSimulationsListing;
            $scope.showSpinKit = true;

            $scope.initialize = function () {
                oxidesSimulationsListingService.getUnicoreJobsListing()
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

oxidesGridPortalApp.value('modelSimulationsListing', []);
