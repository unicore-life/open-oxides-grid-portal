angular
    .module('oxidesGridPortal')
    .factory('oxidesSimulationFilesListingService',

    ['$http', function ($http) {
        return {
            getSimulationFilesList: function (simulationUuid, absoluteUrl) {
                var query = "";
                var queryIndex = absoluteUrl.indexOf("?");
                if (queryIndex >= 0) {
                    query = absoluteUrl.substr(queryIndex);
                }

                var request = {
                    method: 'GET',
                    url: '/unicore/jobs/' + simulationUuid + '/files' + query,
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    data: ''
                };
                return $http(request);
            }
        };
    }]
);
