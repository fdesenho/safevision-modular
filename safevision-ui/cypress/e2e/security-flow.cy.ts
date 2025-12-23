describe('Security Guard Workflow', () => {
  beforeEach(() => {
    // Mock Backend Responses here if testing frontend in isolation
    // Or run against local docker-compose environment
    cy.visit('/login');
  });

  it('should allow login and view dashboard', () => {
    // 1. Login
    cy.get('input[formControlName="username"]').type('admin_safevision');
    cy.get('input[formControlName="password"]').type('123456');
    cy.get('button[type="submit"]').click();

    // 2. Verify Redirect
    cy.url().should('include', '/dashboard');

    // 3. Activate System
    cy.contains('ATIVAR').click();
    cy.get('.status-badge').should('contain', 'ONLINE');

    // 4. Check for Alerts (Mocked or Real)
    // Assuming the python agent sends a test message or we mock the HTTP request
    cy.get('.alerts-list').should('exist');
  });
});