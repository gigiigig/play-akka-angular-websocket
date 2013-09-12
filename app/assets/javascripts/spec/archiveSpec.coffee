baseUrl = "/app"

describe("ArchiveController", ->

  $httpBackend = null
  scope = null
  ctrl = null
  injDialog = null

  beforeEach(module('app'))
  beforeEach(inject((_$httpBackend_, $rootScope, $controller, dialog) ->

      $httpBackend = _$httpBackend_

      $httpBackend.expectGET("#{baseUrl}/task/archive/").respond(tasksArray())

      scope = $rootScope.$new()
      ctrl = $controller('ArchiveController' , {$scope: scope})

      injDialog = dialog
    )
  )

  describe("$scope.updateList", ->
    it("should load a list of tasks", ->

      $httpBackend.expectGET("#{baseUrl}/task/archive/").respond(tasksArray())
      scope.updateList()
      $httpBackend.flush()

      console.debug scope.tasks

      expect(scope.tasks.length).toBe 3

    )
  )  

  describe("$scope.resume(task)", ->
    it("should resume a task", ->

      successMessage = "Task resumed"
      spyOn(scope, 'updateList')
      task = tasksArray()[0]
      $httpBackend.expectPUT("#{baseUrl}/task/archive/#{task.id}").respond(200, successMessage)

      scope.resume(task)
      $httpBackend.flush()

      expect(scope.message).toBe successMessage
      expect(scope.updateList).toHaveBeenCalled()

    )
  )

  describe("$scope.delete(task)", ->
    it("should delete a task", ->

      successMessage = "Task deleted"
      spyOn(scope, 'updateList')
      task = tasksArray()[0]
      $httpBackend.expectDELETE("#{baseUrl}/task/archive/#{task.id}").respond(200, successMessage)

      withShowConfirm(injDialog, $httpBackend, -> scope.delete(task))

      expect(scope.message).toBe successMessage
      expect(scope.updateList).toHaveBeenCalled()

    )
  )


)

tasksArray = ->
  [ 
    {
      closingDate : 1371497806000,
      creationDate : 1371497801000,
      id : 1989,
      name : "wewqe",
      time : 2000
    },
    {
      closingDate : 1371498398000,
      creationDate : 1371497766000,
      id : 1988,
      name : "aaaaaaaaaaaaaaaaa",
      time : 14000
    },
    {
      closingDate : 1371493552000,
      creationDate : -3599000,
      id : 1855,
      name : "#cloudtimr archive",
      time : 6000
    }
  ]
 
withShowConfirm = (injDialog, httpBackend,f) ->
  spyOn(injDialog, 'showConfirm')
  f()
  callback = injDialog.showConfirm.mostRecentCall.args[1]
  callback()
  httpBackend.flush()
