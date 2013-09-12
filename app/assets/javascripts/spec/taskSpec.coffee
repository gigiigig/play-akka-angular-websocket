baseUrl = "/app"

describe('TaskController' , ->

  scope = null
  ctrl = null
  $httpBackend = null
  injDialog = null

  beforeEach(module('app'))
  beforeEach(inject((_$httpBackend_, $rootScope, $controller , time, dialog) ->

    $httpBackend = _$httpBackend_

    $httpBackend.expectGET("#{baseUrl}/task/").respond(tasksArray())
    $httpBackend.whenPUT("#{baseUrl}/task/").respond('Tasks Updated')

    injDialog = dialog
    scope = $rootScope.$new()
    ctrl = $controller('TaskController', {$scope: scope});

    $httpBackend.flush()

  ))

  describe('when start', ->
    it('should load a list of tasks' , ->
      expect(scope.tasks.length).toBe 3
    )
  )

  describe('$scope.start(task)', ->
    it('should set task as running' , ->
      task = new Object()
      scope.start(task)

      expect(task.running).toBe true
    )
  )

  describe('$scope.stop(task)', ->
    it('should set task as not running' , ->
      task = new Object()
      scope.stop(task)

      expect(task.running).toBe false
    )
  )

  describe('$scope.reset(task)', ->
    it('should show confirm set task time to 0 if user accept' , ->

      task = scope.tasks[0]
      expect(task.time).toBe 33556000

      withShowConfirm(injDialog, $httpBackend, -> scope.reset(task))

      expect(task.time).toBe 0
    )
  )

  describe('$scope.add()', ->
    it('should send post with task.name' , ->

      taskName = 'name'
      task = new Object()
      task.name = taskName

      scope.task = new Object()
      scope.task.name = taskName

      spyOn(scope,'update')
      spyOn(scope,'updateList')

      $httpBackend.expectPOST("#{baseUrl}/task/",task).respond(200 , successMessage)

      scope.add()

      $httpBackend.flush()

      expect(scope.message).toBe successMessage
      expect(scope.update).toHaveBeenCalled()
      expect(scope.updateList).toHaveBeenCalled()

    )
  )

  describe('$scope.delete()', ->
    it('should show delete confirm and delete task if user accept' , ->

      task = scope.tasks[0]

      spyOn(scope ,'updateList')
      $httpBackend.expectDELETE("#{baseUrl}/task/#{task.id}").respond(200 , successMessage)

      withShowConfirm(injDialog, $httpBackend, -> scope.delete(task))

      expect(scope.message).toBe successMessage
      expect(scope.updateList).toHaveBeenCalled()

    )
  )

  describe('$scope.resetAll', ->
    it('should show confirm dialog and reset all tasks if user accept and NOT one group il selected', ->

      scope.group = ''

      withShowConfirm(injDialog, $httpBackend, -> scope.resetAll())

      for task in scope.tasks
        expect(task.time).toBe 0

    )

    it('should show confirm dialog and reset all tasks of selected group if user accept and one group is selected', ->

      scope.group = '#test'
      withShowConfirm(injDialog, $httpBackend, -> scope.resetAll())

      for task in scope.tasks
        if task.id == 9
          expect(task.time).toBe 0
        else
          expect(task.time > 0).toBe true
    )
  )

  describe('$scope.deleteAll', ->
    it('should show confirm dialog and delete all tasks if user accept and NOT one group il selected', ->

      #set empty group to avoid that localStoarage group is loaded
      scope.setGroup('')
      spyOn(scope ,'updateList')
      $httpBackend.expectDELETE("#{baseUrl}/task/").respond(200 , successMessage)

      withShowConfirm(injDialog, $httpBackend, -> scope.deleteAll())

      expect(scope.message).toBe successMessage
      expect(scope.updateList).toHaveBeenCalled()

    )

    it('should show confirm dialog delete all tasks of selected group and clean selected group if user accept and one group il selected', ->

      scope.group = '#test'

      spyOn(scope ,'updateList')
      $httpBackend.expectDELETE("#{baseUrl}/task/?group=test").respond(200 , successMessage)

      withShowConfirm(injDialog, $httpBackend, -> scope.deleteAll())

      expect(scope.message).toBe successMessage
      expect(scope.updateList).toHaveBeenCalled()

    )

  )

  describe('$scope.update(callback)', ->
    it('should PUT all tasks to server and call passed callback' , ->

      @callback = -> "ciao"

      spyOn(@,'callback')
      $httpBackend.expectPUT("#{baseUrl}/task/",scope.tasks).respond(200 , successMessage)

      scope.update(@callback)

      $httpBackend.flush()

      expect(@callback).toHaveBeenCalled()
    )
  )

  describe('$scope.prepareTimeEdit = (task,prop)' , ->
    it("
        should crate a field task.'prop'Comp which contains task.'prop'
               field decomposed in seconds, minutes, hours
               formatted with '01' format
       " , ->

      task = new Object()
      task.time = 3661000
      scope.prepareTimeEdit(task , 'time')

      expect(task.timeComp.seconds).toBe '01'
      expect(task.timeComp.minutes).toBe '01'
      expect(task.timeComp.hours).toBe '01'

    )
  )

  describe('$scope.updateTime = (task,prop)' , ->
    it("
        should read from task.'prop'Comp fields seconds, minutes , hours
               and transform them in a millisecond value and set to task.'prop'
       " , ->

      task = {
       timeComp : new Object()
      }
      task.timeComp.seconds = '01'
      task.timeComp.minutes = '01'
      task.timeComp.hours = '01'

      scope.updateTime(task ,'time')

      expect(task.time).toBe 3661000

    )
    it("should not edit time if inseted numbers aren't valid" , ->
      task = {
       timeComp : new Object(),
       time : 12
      }
      task.timeComp.seconds = 'ed'
      task.timeComp.minutes = '01'
      task.timeComp.hours = '01'

      scope.updateTime(task ,'time')

      expect(task.time).toBe 12
      expect(scope.message).toBeDefined()
    )
  )

  describe('$scope.total', ->
    it('should return the total time of all tasks' , ->
      expect(scope.total()).toBe(33556000 + 5755000 + 2223443)
    )
  )

)

describe("StatsController", ->  

  scope = null
  ctrl = null
  $httpBackend = null
  injDialog = null

  beforeEach(module('app'))
  beforeEach(inject((_$httpBackend_, $rootScope, $controller , time, dialog) ->

    $httpBackend = _$httpBackend_

    $httpBackend.whenGET("#{baseUrl}/task/").respond(tasksArray())

    injDialog = dialog
    scope = $rootScope.$new()
    ctrl = $controller('StatsController', {$scope: scope});

    $httpBackend.flush()

  ))

  describe("when start", ->
    
    it("should start with right values", ->
      
      expect(scope.type).toBe 'task'
      expect(scope.source).toBe 'tasks'
      expect(scope.group).toBe ''
      expect(scope.graphType).toBe 'bar'
    
    )  

  )

  describe('watchers', ->

    describe("changing values of 'type', 'source', 'group' load right data for following combinations", ->

      it("'type' == 'group' && 'source' == 'tasks'", ->

        $httpBackend.expectGET("#{baseUrl}/task/").respond(tasksArray())
        scope.type = 'group'
        scope.$apply()
        $httpBackend.flush()
        expect(scope.data).toEqual groupArray()

      )
      
      it("'type' == 'group' && 'source' == 'archive'", ->

        $httpBackend.whenGET("#{baseUrl}/task/").respond(archiveArray())
        $httpBackend.whenGET("#{baseUrl}/task/archive/").respond(archiveArray())
        scope.type = 'group'
        scope.source = 'archive'        
        scope.$apply()
        $httpBackend.flush()
        expect(scope.data).toEqual groupArchiveArray()

      )
    )
  )

  groupArray = -> [ { name : '#test', time : 2223443 } ]
  groupArchiveArray = -> [ { name : '#testarchive', time : 2223443 } ]
)


successMessage = 'Success message'

tasksArray = ->
  [
    {
      id: 8,
      name: "ciao",
      time: 33556000,
      goal: 33000,
      running: false
    },
    {
      id: 7,
      name: "Task without goal",
      time: 5755000,
      goal: 0,
      running: false
    },
    {
      id: 9,
      name: "#test Task without goal",
      time: 2223443,
      goal: 0,
      running: false
    }
  ]

archiveArray = ->
  [
    {
      id: 1,
      name: "archive task 1",
      time: 33556000,
      goal: 33000,
      running: false
    },
    {
      id: 7,
      name: "archive task 2",
      time: 5755000,
      goal: 0,
      running: false
    },
    {
      id: 9,
      name: "#testArchive Task without goal",
      time: 2223443,
      goal: 0,
      running: false
    }
  ]

withShowConfirm = (injDialog, httpBackend,f) ->
  spyOn(injDialog, 'showConfirm')
  f()
  callback = injDialog.showConfirm.mostRecentCall.args[1]
  callback()
  httpBackend.flush()
