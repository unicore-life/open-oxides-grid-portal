angular
    .module('oxidesGridPortal')
    .factory('oxidesSimulationsListingService',

    ['$http', function ($http) {
        return {
            getUnicoreJobsListing: function () {
                var token = $("meta[name='_csrf']").attr("content");
                var header = $("meta[name='_csrf_header']").attr("content");
                $http.defaults.headers.common[header] = token;

                return $http.get('/unicore/jobs', {
                    headers: {'Content-Type': 'application/json'},
                    data: ''
                });
            }
        };
    }]
);
