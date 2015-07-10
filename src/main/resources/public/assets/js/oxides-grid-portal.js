var oxidesGridPortalApp = angular.module('oxidesGridPortal', [
    'feeds',
    'ngRoute',
    'ui.bootstrap'
]);


oxidesGridPortalApp.controller('oxidesGridPortalController', function ($scope) {
  $scope.phones = [
    {'name': 'Nexus S',
     'snippet': 'Fast just got faster with Nexus S.'},
    {'name': 'Motorola XOOM™ with Wi-Fi',
     'snippet': 'The Next, Next Generation tablet.'},
    {'name': 'MOTOROLA XOOM™',
     'snippet': 'The Next, Next Generation tablet.'}
  ];

  $scope.yourName = null;
});

oxidesGridPortalApp.controller('oxidesSimulationsListingController',
    function ($scope, oxidesSimulationsListingService, modelSimulationsListing) {
        $scope.simulations = modelSimulationsListing;

        oxidesSimulationsListingService.getJson()
            .success(function(data, status, headers, config) {
                angular.copy(data, modelSimulationsListing);
            })
            .error(function(data, status, headers, config) {
                alert('Failed: HTTP Status Code = ' + status);
            });
    }
);


oxidesGridPortalApp.factory('oxidesSimulationsListingService',
    ['$http', function ($http) {
        return {
            getJson: function () {
                return $http.get('/oxides/unicore-jobs');
            }
        };
    }]
);


oxidesGridPortalApp.value('modelSimulationsListing', []);

