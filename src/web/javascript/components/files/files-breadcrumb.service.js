angular
    .module('oxidesGridPortal')
    .factory('oxidesSimulationFilePathBreadCrumbService',

    [function () {
        return {
            getBreadCrumbElementsList: function (path, locationUrl) {
                var breadCrumbElements = [];

                breadCrumbElements.push({
                    label: 'Simulation Directory',
                    href: locationUrl
                });

                var pathElements = decodeURIComponent(path).split('/');
                var pathSoFar = '/';
                for (var i = 0; i < pathElements.length; i++) {
                    if (pathElements[i] !== '') {
                        pathSoFar += (pathElements[i] + '/');
                        breadCrumbElements.push({
                            label: pathElements[i],
                            href: locationUrl + '?path=' + pathSoFar
                        });
                    }
                }
                return breadCrumbElements;
            }
        };
    }]
);
