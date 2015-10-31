angular
    .module('oxidesGridPortal')
    .controller('oxidesMoleculeViewerController',

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
