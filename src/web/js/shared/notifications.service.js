angular
    .module('oxidesGridPortal')
    .factory('oxidesPopMessageHandlingService',

    ['toaster', function (toaster) {
        return {
            handleError: function (response) {
                var toasterMessage = response.statusText;
                if (response.status === 0 && toasterMessage === '') {
                    toasterMessage = 'No connection or timeout/cancel occurred.';
                }
                if (response.data) {
                    toasterMessage += '. ' + (response.data.message || '');
                }

                toaster.pop('error', 'Request failed (' + response.status + ')', toasterMessage);

                console.error('Failed: HTTP Status Code = ' + response.status);
            },

            handleDeletionSuccess: function (response, uuid, idx) {
                console.info('Deleting simulation ' + uuid + ' (' + idx + ') accepted.');
            },

            handleSubmissionSuccess: function (response, oxidesSimulation) {
                toaster.pop('info', 'Simulation submitted (' + response.status + ')',
                    'Simulation "' + oxidesSimulation.name + '" sent to UNICORE broker.');

                console.info('Submitted: HTTP Status Code = ' + response.status);
            }
        };
    }]
);
