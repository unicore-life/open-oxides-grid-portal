var oxidesGridPortalApp = angular.module('oxidesGridPortal', [
    'feeds',
    'ngRoute',
    'ui.bootstrap'
]);

//oxidesGridPortalApp.controller('oxidesGridPortalController', function ($scope, dataService, modelService) {
oxidesGridPortalApp.controller('oxidesGridPortalController', function ($scope, dataService) {
    $scope.phones = [
        {'name': 'Nexus S',
             'snippet': 'Fast just got faster with Nexus S.'},
        {'name': 'Motorola XOOM™ with Wi-Fi',
             'snippet': 'The Next, Next Generation tablet.'},
        {'name': 'MOTOROLA XOOM™',
             'snippet': 'The Next, Next Generation tablet.'}
    ];

    $scope.yourName = null;
//    $scope.myData = modelService;
    $scope.myData = [];

//    dataService.getJson()
//        .then(
//            function (result) {
//                angular.copy(result.data, $scope.myData);
//            })
//        .catch(
//            function(reason) {
//                alert('Failed: ' + reason);
//            });
    dataService.getJson()
        .success(function(data, status, headers, config) {
            angular.copy(data, $scope.myData);
        })
        .error(function(data, status, headers, config) {
//            alert('Failed: HTTP Status Code = ' + status);
            console.log('Failed: HTTP Status Code = ' + status);
        });
});

oxidesGridPortalApp.factory('dataService', ['$http', function ($http) {
    return {
        getJson: function () {
//            return $http.get('/testing');
            return $http.get('/unauthorized');
        }
    };
}]);

//oxidesGridPortalApp.value('modelService', []);


oxidesGridPortalApp.controller('oxidesSubmitSimulationController', function ($scope) {
    $scope.title = null;

    $scope.resetForm = function () {
        $scope.title = 'x';

        $scope.detailsForm.$setPristine();
    };
});
