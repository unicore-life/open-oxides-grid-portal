angular
    .module('oxidesGridPortal')
    .controller('oxidesSimulationDetailsController',

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
