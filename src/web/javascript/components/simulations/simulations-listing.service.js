angular
    .module('oxidesGridPortal')
    .factory('oxidesSimulationsListingService',

    ['$http', function ($http) {
        return {
            getUnicoreJobsListing: function () {
                return $http.get('/unicore/jobs', {
                    headers: {'Content-Type': 'application/json'},
                    data: ''
                });
            }
        };
    }]
);
