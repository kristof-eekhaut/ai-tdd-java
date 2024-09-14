# AI TDD Java coder

## Plan
1. Generate tests for description or provide tests as input
2. Generate code
3. Compile code and tests 
    - if compilation fails:
      - Reason about errors
      - Regenerate code (and tests) given reasoning
4. Run tests
    - if tests fail:
      - Reason about errors
      - Regenerate code (and tests) given reasoning
5. All tests pass = done

Note: in the case where the tests are provided by the user, it should not change/fix the tests. 
It should just give feedback to the user if it can't fix the issue after a max nr of attempts.