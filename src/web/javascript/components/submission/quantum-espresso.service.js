angular
    .module('oxidesGridPortal')
    .factory('oxidesSubmitQuantumEspressoSimulationService',

    ['$http', function ($http) {
        return {
            submitSimulation: function (oxidesSimulationJson) {
                var token = $("meta[name='_csrf']").attr("content");
                var header = $("meta[name='_csrf_header']").attr("content");
                var request = {
                    method: 'POST',
                    url: '/unicore/submit/qe',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    data: oxidesSimulationJson
                };
                $http.defaults.headers.common[header] = token;
                return $http(request);
            }
        };
    }]
);
