The code is well-written and easy to read.

I appreciate the use of data classes.

The controller and service layers are well separated. However, DTO and Entity objects are not clearly distinguished. While separating them is generally a good practice, in this case, it’s not a major issue since there is no database persistence involved.

Tests are present, and the code coverage is good. However, some frontend tests seem incorrect, as they did not identify an existing issue in the application.

One issue is with exception handling—exceptions should extend Exception or RuntimeException, not Throwable. Additionally, a global exception handler would improve error management.

There are some minor clean code violations, but nothing critical.

Logging is quite limited; adding more logging would improve observability and debugging.

Great use of Lombok! It helps keep the code clean and reduces unnecessary boilerplate, making it more readable and maintainable.

The biggest issue was that the program did not function correctly. It should display the maximum loan amount that can be approved, but instead, it showed the requested amount when it was smaller than the maximum approved amount. This issue has now been fixed to ensure the program behaves as expected.

Not all constraints have been followed 100%, such as the maximum loan period. I used the constraints that were already in the code instead of those specified in TICKET-102