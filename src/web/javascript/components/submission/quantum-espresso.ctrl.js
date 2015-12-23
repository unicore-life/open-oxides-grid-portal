angular
    .module('oxidesGridPortal')
    .controller('oxidesSubmitQuantumEspressoSimulationController',

        ['$scope', 'FileUploader', 'oxidesSubmitQuantumEspressoSimulationService', 'oxidesPopMessageHandlingService',
            function ($scope, FileUploader,
                      oxidesSubmitQuantumEspressoSimulationService,
                      oxidesPopMessageHandlingService) {
                $scope.simulationParameters = {
                    simulationName: '',
                    simulationProject: '',
                    simulationQueue: '',
                    simulationMemory: '',
                    simulationNodes: '',
                    simulationCPUs: '',
                    simulationReservation: '',
                    simulationProperty: 'westmere'
                };
                $scope.simulationScriptText = '';
                $scope.simulationUploadFilesList = [];

                $scope.simulationFormFields = [
                    {'id': 'simulationName', 'label': 'Name', 'placeholder': 'Simulation Name'},
                    {'id': 'simulationProject', 'label': 'Project', 'placeholder': 'Grant ID'},
                    {'id': 'simulationQueue', 'label': 'Queue', 'placeholder': 'Queue'},
                    {'id': 'simulationMemory', 'label': 'Memory', 'placeholder': 'Memory [MB]'},
                    {'id': 'simulationNodes', 'label': 'Nodes Count', 'placeholder': 'Number of nodes'},
                    {'id': 'simulationCPUs', 'label': 'CPUs / Node', 'placeholder': 'CPUs per node'},
                    {'id': 'simulationReservation', 'label': 'Reservation', 'placeholder': 'Reservation ID'},
                    {'id': 'simulationProperty', 'label': 'Nodes Property', 'placeholder': 'Nodes property string'}
                ];

                $scope.isSubmitting = undefined;
                $scope.toggleAdvancedValue = false;
                $scope.toggleAdvancedLabel = 'Show Advanced Parameters';


                $scope.toggleAdvanced = function () {
                    var prefix = $scope.toggleAdvancedLabel.substr(0, 4).toLowerCase();
                    var toggledPrefix = (prefix === 'show') ? 'Hide' : 'Show';

                    $scope.toggleAdvancedLabel = toggledPrefix + ' Advanced Parameters';
                    $scope.toggleAdvancedValue = !$scope.toggleAdvancedValue;
                };

                $scope.isParameterRequired = function (parameterName) {
                    return parameterName === 'simulationName';
                };

                $scope.isAdvancedVisible = function (parameterName) {
                    return (parameterName === 'simulationProject')
                        || $scope.isParameterRequired(parameterName)
                        || $scope.toggleAdvancedValue;
                };

                $scope.resetForm = function () {
                    $scope.simulationSubmitForm.$setPristine();
                };

                $scope.submitForm = function () {
                    $scope.isSubmitting = true;

                    // Creates JSON with simulation description:
                    var oxidesSimulation = {};
                    var offset = 'simulation'.length;
                    $scope.simulationFormFields.map(function (it) {
                        var parameterValue = $scope.simulationParameters[it.id];
                        if (parameterValue !== null && parameterValue !== '') {
                            var parameterName = it.id.substr(offset).toLowerCase();
                            oxidesSimulation[parameterName] = parameterValue;
                        }
                    });
                    oxidesSimulation.script = $scope.simulationScriptText;
                    oxidesSimulation.files = $scope.simulationUploadFilesList;
                    var oxidesSimulationJson = JSON.stringify(oxidesSimulation);

                    // Submits simulation:
                    oxidesSubmitQuantumEspressoSimulationService
                        .submitSimulation(oxidesSimulationJson)
                        .then(function (response) {
                            oxidesPopMessageHandlingService.handleSubmissionSuccess(response, oxidesSimulation);
                            $scope.isSubmitting = false;
                        })
                        .catch(function (response) {
                            oxidesPopMessageHandlingService.handleError(response);
                            $scope.isSubmitting = false;
                        });
                };

                $scope.removeUploadFile = function (idx) {
                    $scope.simulationUploadFilesList.splice(idx, 1);
                };

                $scope.uploader = new FileUploader({
                    url: '/unicore/upload',
                    alias: 'uploadFile',
                    headers: {
                        // FIXME: the name of header should be also read from meta
                        'X-CSRF-TOKEN': $("meta[name='_csrf']").attr("content")
                    },
                    queueLimit: 1,
                    autoUpload: true,
                    removeAfterUpload: true,

                    onSuccessItem: function (item, response, status, headers) {
                        var filename = item.file.name;
                        var arrayLength = $scope.simulationUploadFilesList.length;
                        for (var i = arrayLength - 1; i >= 0; i--) {
                            if ($scope.simulationUploadFilesList[i] === filename) {
                                $scope.simulationUploadFilesList.splice(i, 1);
                            }
                        }
                        $scope.simulationUploadFilesList.push(filename);
                    },

                    onCompleteItem: function (item, response, status, headers) {
                        this.clearQueue();

                        // Workaround for re-upload:
                        $scope.uploader._directives.select[0].element[0].value = '';
                    }
                });
            }
        ]
    );
