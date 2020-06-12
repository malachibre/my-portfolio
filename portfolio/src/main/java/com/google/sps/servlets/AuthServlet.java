//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;



import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * This class uses the Authentitication API to check whehter a user is 
 * logged in or not. And, also gives them tha ability to login/out.
 */
@WebServlet("/auth")
public final class AuthServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      
    response.setContentType("text/html");

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      response.getWriter().println("logged in as " + userService.getCurrentUser().getEmail());

      String urlRedirectAfterLogout = "/";
      String logoutUrl = userService.createLogoutURL(urlRedirectAfterLogout);

      response.getWriter().println("<p>Logout <a href=\"" + logoutUrl + "\">here</a>.</p>");
    } else {
      String urlRedirectAfterLogin = "/";
      String loginUrl = userService.createLoginURL(urlRedirectAfterLogin);

      response.getWriter().println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");

    }
  }
}