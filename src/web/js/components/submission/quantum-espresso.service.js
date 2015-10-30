angular
    .module('oxidesGridPortal')
    .factory('oxidesSubmitQuantumEspressoSimulationService',

    ['$http', function ($http) {
        return {
            submitSimulation: function (oxidesSimulationJson) {
                var request = {
                    method: 'POST',
                    url: '/unicore/submit/qe',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    data: oxidesSimulationJson
                };
                return $http(request);
            }
        };
    }]
);
